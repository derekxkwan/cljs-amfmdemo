# cljs-amfmdemo

- working, interactive demo on ring modulation, amplitude modulation, and frequency modulation synthesis using `clojurescript`,`reagent`, and `Web Audio API`
- compiled version is found in the `docs/` folder (or [https://derekxkwan.github.io/cljs-amfmdemo/](https://derekxkwan.github.io/cljs-amfmdemo/))

## Progress

- working demo (6-22-2018: now with different oscillator types!)

## To Do

- allow selection of different types of oscillators for carrier / ring modulator / amplitude modulator / frequency modulator

## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## License

gpl v. 3
