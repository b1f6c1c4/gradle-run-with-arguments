package com.github.b1f6c1c4.gradle_run_with_arguments;

public class AddJarConfiguration extends AddConfiguration {

    @Override
    protected String getMessage() {
        return "\nDon't forget to Patch your gradle-wrapper.jar!";
    }

    @Override
    protected IConfigurer getConfigurer() {
        return new JarConfigurer();
    }
}
