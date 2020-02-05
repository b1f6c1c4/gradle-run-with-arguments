# gradle-run-with-arguments

> Fix gradle run, and add --args to it

## Why

### What's wrong with the IntelliJ IDEA's Gradle plugin

There is a long-standing bug that `System.out.print` will not flush to the `Run` panel in IntelliJ IDEA.
See the following links for more information:

* [IntelliJ support](https://intellij-support.jetbrains.com/hc/en-us/community/posts/360004966559-System-out-print-TEST-don-t-display-on-the-console-in-IntelliJ)
* [JetBrains YouTrack](https://youtrack.jetbrains.com/issue/IDEA-184090?_ga=2.138301045.470666916.1580757752-819754429.1579503100)
* [Gradle discussion](https://discuss.gradle.org/t/gradle-print-vs-println/21334)
* [Stackoverflow](https://stackoverflow.com/questions/58406613/system-out-printcalculator-doesnt-show-an-output-when-it-is-followed-with)

This bug hasn't been fixed for years and we can't wait infinitely for this.
So I wrote this plugin (specifically v2.0 and above) as a workaround.
How this plugin works will be explained lateron.

### What's wrong with the Gradle application plugin

The Gradle application plugin requires that you pass the command line arguments in the `--args` task property.
This becomes troublesome when using IntelliJ IDEA, in which the task properties are buried deep in the menu.
It's a total nightmare if you are debuggin by changing the arguments.
So I wrote this plugin to automatically prompt for the arguments and change the `--args` task property accordingly.

## How (TL;DR)

You just install the IntelliJ IDEA plugin `Gradle Run with Arguments`.
Remove all existing configurations.
Click the blue triangle to create a run configuration (choose JAR configuration if you want to use fix the `System.out.print` problem).
Click the blue triangle again to modify your command line arguments.

## Warning

Using this plugin will **destroy** all the script argument settings of your gradle run configuration.
Only use this plugin on its *own* run configurations.

## Limitation

Does not support IntelliJ IDEA debugging.
