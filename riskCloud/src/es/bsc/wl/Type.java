/*
    Author: Mario Macias, 2013

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License Version 2, as
    published by the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>
*/
package es.bsc.wl;

/**
 * Represents an OCCI type, but also stores SLA information
 */
public class Type implements Cloneable {

    private String id;

    private double resourceUnits;
    private final int startTime;
    private final int duration;
    private Resource owningResource;

    public Type(String id, double resourceUnits, int startTime, int duration) {
        this.id = id;
        this.resourceUnits = resourceUnits;
        this.duration = duration;
        this.startTime = startTime;
    }

    public String getId() {
        return id;
    }

    public double getResourceUnits() {
        return resourceUnits;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setOwningResource(Resource owningResource) {
        this.owningResource = owningResource;
    }

    public Resource getOwningResource() {
        return owningResource;
    }


    public double getReservationPrice() {

        double density = resourceUnits / owningResource.accountingGroup.totalResourceUnits;
        double toAmortize = owningResource.accountingGroup.TCO - owningResource.accountingGroup.totalAmortized;
        double restOfLifeTime = owningResource.accountingGroup.maxLifeTime - (owningResource.accountingGroup.currentTime - owningResource.accountingGroup.insertionTime);

        double rp = toAmortize * duration * density / (restOfLifeTime * owningResource.getProvider().getHistoricWorkload());
        return Math.max(0, rp);
    }

    @Override
    public String toString() {
        return "Type{" +
                "id='" + id + '\'' +
                '}';
    }

    @Override
    public Object clone() {
        Type t = new Type(id,resourceUnits,startTime,duration);
        t.owningResource = owningResource;
        return t;
    }
}
