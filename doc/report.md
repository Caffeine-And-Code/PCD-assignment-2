# Documentazione

## Autori

- Mattia Senni (mattia.senni@studio.unibo.it)
- Fabio Fattori (fabio.fattori3@studio.unibo.it)
- Francesco Tonelli (francesco.tonelli9@studio.unibo.it)

## Analisi

Durante l'analisi del progetto, sono stati riscontrati i seguenti aspetti critici riguardo allo sviluppo del progetto:
- una directory forma un albero di profondità e ampiezza non note a priori. Le sottodirectory sono indipendenti e potrebbero, quindi, essere visitate concorrentemente, ma un numero alto di worker autonomi potrebbe non garantire un miglioramento delle prestazioni, perché tutte le attività accedono allo stesso filesystem;
- la scansione può richiedere un tempo non prevedibile. Il metodo creato deve quindi restituire il controllo al chiamante senza attendere il completamento dell'intera visita;
- se più attività producono risultati contemporaneamente, gli aggiornamenti devono essere coordinati per evitare incrementi persi, duplicazioni o report parziali incoerenti;
- la conclusione dell'operazione iniziale non coincide necessariamente con quella delle attività generate ricorsivamente. Il report finale può essere restituito solo quando ogni ramo avviato è terminato;
- un utente deve poter leggere il risultato mentre la scansione continua. Ogni aggiornamento pubblicato deve, quindi, rappresentare uno stato consistente, anche se l'ordine di completamento dei rami non è deterministico;
- la richiesta di arresto può arrivare mentre sono presenti attività ancora in corso. Questa deve, perciò, essere propagata al lavoro ancora attivo e deve impedire, per quanto possibile, l'avvio di nuove operazioni.

## Soluzioni adottate

Le tre versioni condividono solamente `FSReport`, il quale costituisce il record di dati emesso dalla funzione richiesta, per facilitare lo sviluppo della lettura da parte della GUI. 
Questo report è immutabile, perciò l'elaborazione di un file produce un nuovo valore, così gli aggiornamenti intermedi possono essere osservati senza esporre uno stato in corso di modifica.

In ognuna delle tre implementazioni è stato inoltre specificato che i link simbolici non venissero seguiti, evitando cicli, duplicazioni e visite esterne alla directory scelta, soprattutto in sistemi macOS.

### Versione Event-Loop

La versione asincrona usa il filesystem di Vert.x, con `readDir` e `lprops` che permettono di eseguire le operazioni necessarie senza bloccare l'event loop. 
- La visita ricorsiva è espressa tramite composizione di `Future`, i quali risultati vengono collegati tramite `compose` alla continuazione successiva, mentre `Future.all` consente di dichiarare conclusa una directory solo dopo il completamento di tutti i suoi rami. 
- Lo stato è locale alla singola richiesta, così scansioni diverse non interferiscono tra loro. 
- Dopo ogni file viene pubblicato un nuovo report tramite `FSStatSession`. 
- Il callback dell'utente è eseguito come operazione bloccante separata, perché il suo tempo di esecuzione non è controllabile dalla libreria e non deve rallentare l'esecuzione totale. 
- La cancellazione della ricerca è realizzata tramite un flag, il quale viene verificato durante la visita per evitare di avviare nuovo lavoro, senza tentare di interrompere operazioni asincrone già consegnate al filesystem.

### Versione Reactive

La versione reattiva restituisce un `Flowable` di tipo cold, perché ogni sottoscrizione deve rappresentare una nuova scansione indipendente e il lavoro non deve iniziare prima che esista un consumatore. `Files.walk` produce in maniera lazy i percorsi, mentre gli operatori li filtrano e trasformano. Infine `scan` accumula ed emette un nuovo `FSReport` dopo ogni file. 
`Flowable.using` lega la risorsa `Stream` alla sottoscrizione e ne garantisce la chiusura anche in caso di errore o cancellazione.

È stato scelto `Flowable` per mantenere la backpressure tra sorgente e subscriber. L'intera pipeline viene spostata su un worker di `Schedulers.io()` per non bloccare il chiamante. 

Non è stato introdotto ulteriore parallelismo perché `Files.walk` è una sorgente sequenziale, e accessi concorrenti allo stesso filesystem aggiungerebbero coordinamento senza un vantaggio garantito (come detto nell'analisi, le dimensioni della cartella non sono note a priori, e un alto numero di worker non garantirebbe un aumento delle performance). 

Infine, la cancellazione è rappresentata come un ulteriore flusso: `takeUntil` termina la pipeline quando la sessione emette il segnale di arresto.

### Versione Virtual Threads

La terza versione usa un `newVirtualThreadPerTaskExecutor`. I virtual thread consentono di mantenere una visita ricorsiva semplice, basata sulle API bloccanti di `java.nio.file`, senza associare a ogni sottodirectory il costo di un platform thread. Ogni sottodirectory diventa quindi un'attività separata e può avanzare indipendentemente dalle altre.

Queste attività condividono il risultato, perciò `FSReportMonitor` protegge con un `ReentrantLock` le operazioni di lettura, aggiornamento e sostituzione del report. Ogni directory attende i `Future` dei figli prima di terminare, garantendo che il risultato finale venga restituito solo dopo la visita dell'intero albero. 

La cancellazione usa l'interruzione del `Future` principale. In caso di arresto, ciascuna attività cancella i figli già avviati e propaga l'interruzione verso l'alto.

### GUI

L'estensione JavaFX rende osservabili le funzionalità opzionali richieste, ovvero aggiornamenti progressivi e arresto della scansione. Un adattatore separato per ogni versione conserva il relativo meccanismo di esecuzione e cancellazione. L'attesa del risultato avviene in un `Task`, così il thread JavaFX rimane sempre reattivo. Gli aggiornamenti della vista passano invece da `Platform.runLater`, come richiesto dal modello a thread singolo dell'interfaccia. 
