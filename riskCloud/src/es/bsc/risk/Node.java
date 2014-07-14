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
package es.bsc.risk;

import es.bsc.wl.Type;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class Node extends AbstractNode {
    private Link link = null;
    private Type type;

    public Node(Type type) {
        this.type = type;
    }


    public Node(Type type, Link link) {
        this.link = link;
        this.type = type;
    }

    /**
     * Returns the PoF for a time slot, calculated as the maximum value of the PoF function for such time slot
     * @return
     */
    public double getNodePoF(Set<Type> allTypes) {
        int startTime = type.getStartTime();
        int duration = type.getDuration();
        return type.getOwningResource().getRisk(startTime,duration,allTypes,true);
    }

    @Override
    public double getPropagatedPoF(Set<Type> allTypes) {
        final double np = getNodePoF(allTypes);
        if(link == null) {
            return np;
        } else {
            final double lw = link.getWeight();
            final double pp = link.getDestinationNode().getPropagatedPoF(allTypes);
            return np + lw * pp - lw * np * pp;
        }
    }

    @Override
    public Set<Type> getConnectedTypes() {
        Set<Type> ct = new HashSet<Type>();
        ct.add(type);
        if(link != null) {
            ct.addAll(link.getDestinationNode().getConnectedTypes());
        }
        return ct;
    }

    public Type getType() {
        return type;
    }

    @Override
    public Object clone() {
        Link l = null;
        if(link != null) {
            l = (Link)link.clone();
        }
        return new Node((Type)type.clone(),l);
    }



}
