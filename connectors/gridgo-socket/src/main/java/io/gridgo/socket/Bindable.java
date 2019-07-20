package io.gridgo.socket;

public interface Bindable extends HasBindingPort {

    void bind(String address);
}
