package net.sharksystem;

public class ExampleYourComponentListener implements YourComponentListener {
    public int counter = 0;

    @Override
    public void somethingHappenedFormatA(CharSequence message) {
        System.out.println("A happened: " + message);
        this.counter++;
    }
}
