package uk.al_richard.experimental.angles.MarginBlaster;

import java.util.List;

public abstract class SheetExclusionModded<T> extends ExclusionZoneModded<T> {
    public abstract void setWitnesses(List<T> witnesses);

    public abstract RefPointSet<T> getPointSet();

    public abstract int getRef1();

    public abstract int getRef2();

    public abstract double getInterPivotDistance();


}
