# Documentazione

## Autori
- Mattia Senni (mattia.senni@studio.unibo.it)
- Fabio Fattori (fabio.fattori3@studio.unibo.it)
- Francesco Tonelli (francesco.tonelli9@studio.unibo.it)

## Analisi

## Soluzioni adottate

### Versione Event-Loop

### Versione Reactive

Uso della libreria dall'esterno:

```java
import io.reactivex.rxjava3.disposables.Disposable;
import pcd.reactiveprogramming.FSStatLib;
import pcd.reactiveprogramming.FSStatLibReactive;
import pcd.reactiveprogramming.FSStatSession;

FSStatLib library = new FSStatLibReactive();
FSStatSession session = new FSStatSession();

Disposable subscription = library
        .getFSReport("test-data", 100, 4, session)
        .subscribe(
                System.out::println,
                Throwable::printStackTrace,
                () -> System.out.println("Scansione terminata")
        );

// Ad esempio, richiamato dall'azione del pulsante Stop.
session.cancel();
```

`getFSReport` restituisce un `Flowable` freddo: la scansione parte al momento
della sottoscrizione. Ogni elemento emesso è un report aggiornato e
`session.cancel()` invia alla pipeline l'evento che ne richiede la terminazione.

- La scansione usa un solo worker `Schedulers.io()`: il requisito richiede che `getFSReport` sia asincrono rispetto al chiamante, non che i file siano elaborati in parallelo. La GUI rimane quindi responsiva mentre l'intera pipeline lavora in background.
- `Files.walk()` fornisce una sorgente lazy e sequenziale. Aggiungere worker a valle non renderebbe parallela l'esplorazione delle directory, ma soltanto alcune operazioni sui file già prodotti, introducendo merge, riordinamento e task in-flight senza un beneficio richiesto o misurato.
- Il worker singolo conserva una catena diretta di backpressure tra subscriber e sorgente. In caso di cancellazione non rimangono più operazioni `Files.size()` concorrenti da attendere: la sottoscrizione viene interrotta e `Flowable.using()` chiude lo stream di `Files.walk()`.
- Il parallelismo resta implementabile con `flatMap` e sottoscrizioni interne schedulate, come mostrato nel laboratorio, ma è stato escluso intenzionalmente: il collo di bottiglia è il filesystem e accessi concorrenti non garantiscono prestazioni migliori su tutti i sistemi operativi e dispositivi.

### Versione Virtual Threads

### GUI