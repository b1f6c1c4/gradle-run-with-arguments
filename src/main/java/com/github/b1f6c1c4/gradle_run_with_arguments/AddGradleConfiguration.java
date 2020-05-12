package com.github.b1f6c1c4.gradle_run_with_arguments;

public class AddGradleConfiguration extends AddConfiguration {

    @Override
    protected String getMessage() {
        return "";
    }

    @Override
    protected IConfigurer getConfigurer() {
        return new GradleConfigurer();
    }
}
