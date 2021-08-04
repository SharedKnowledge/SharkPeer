package net.sharksystem;

public class ExampleYourComponentListener implements YourComponentListener {
    public int counter = 0;

    @Override
    public void somethingHappened(CharSequence message) {
        System.out.println("message received: " + message);
        this.counter++;
    }
}
