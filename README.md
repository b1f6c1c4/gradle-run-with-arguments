# gradle-run-with-arguments

> IntelliJ IDEA plugin: `gradlew run --args "..."`; `System.out.print(...);`

## Why

### What's wrong with the Gradle application plugin

The Gradle application plugin requires that you pass the command line arguments in the `--args` task property.
This becomes troublesome when using IntelliJ IDEA, in which the task properties are buried deep in the menu.
It's a total nightmare if you are debugging by changing the arguments.
So I wrote this plugin to automatically prompt for the arguments and change the `--args` task property accordingly.

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

## How (TL;DR)

1. **You should first install the IntelliJ IDEA plugin `Gradle Run with Arguments`.**
Open `Settings`/`Plugin` (or `Preferences`/`Plugin`).
Type the name in the search box.
Install.
1. Decide if you want to use a JAR configuration or a Gradle configuration.
    * Choose JAR if you want to use `System.out.print` but not the built-in debugging functionality for gradle.
    * Choose Gradle if you want to use the built-in debugging functionality for gradle but not `System.out.print`.
1. If you've chosen JAR configuration, you need to **patch your `gradle/wrapper/gradle-wrapper.jar` file** by right-click on it and select `Patch for "Run with Arguments"`.
This step is **required** as long as you are using JAR configuration.
1. If you don't have a configuration yet, just click the blue triangle next to the `Build Project` button next to the configuration combo in the toolbar run group.
It will prompt you for creating a new configuration.
Click the blue triangle again to modify your command line arguments.
1. If you already have a configuration and you want to add another one, just click `Run`/`Run with Arguments`/`Add XXX Configuration`.

## Warning

Using this plugin is likely to **destroy** all the script argument settings of your run configuration(s).
Only use this plugin on its *own* run configurations.

## Common Problems

### Quoting is not behaving properly

Consider using single quotes (`'`) instead of (`"`) when introducing argument(s) with spaces.
However, there is no way to escape quotes.

### `no main manifest attribute`

RTFM - patch your `gradle/wrapper/gradle-wrapper.jar` file by right-click on it and select `Patch for "Run with Arguments"`.

## Limitation

* There's currently no way to support `System.out.print` while supporting in IntelliJ IDEA built-in debugging functionality at the same time.
* There's currently no way to escape a quote (`'` or `"`) in the argument.
* There's currently no way to use `"` instead of `'` to group the arguments.

