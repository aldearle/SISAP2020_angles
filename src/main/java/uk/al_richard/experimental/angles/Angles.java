package uk.al_richard.experimental.angles;

public class Angles {
    public double angle;
    public double std_dev;
    public int angles_measured;

    public Angles(double angle, double std_dev, int num_angles) {
        this.angle = angle;
        this.std_dev = std_dev;
        this.angles_measured = num_angles;
    }
}
