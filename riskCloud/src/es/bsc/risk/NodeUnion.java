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

public class NodeUnion extends AbstractNode {
    private Link[] links = null;

    public NodeUnion(Link[] links) {
        if(links == null || links.length < 2 || links.length > 4) {
            throw new IllegalArgumentException("Links size must be >=2 and <=4");
        }
        this.links = links;
    }

    @Override
    public double getPropagatedPoF(Set<Type> allTypes) {
        if(links.length == 2) {
            double p0 = links[0].getWeight()* links[0].getDestinationNode().getPropagatedPoF(allTypes);
            double p1 = links[1].getWeight()* links[1].getDestinationNode().getPropagatedPoF(allTypes);
            return p0 + p1 - p0 * p1;
        } else if(links.length == 3) {
            double p0 = links[0].getWeight()* links[0].getDestinationNode().getPropagatedPoF(allTypes);
            double p1 = links[1].getWeight()* links[1].getDestinationNode().getPropagatedPoF(allTypes);
            double p2 = links[2].getWeight()* links[2].getDestinationNode().getPropagatedPoF(allTypes);

            return p0 + p1 + p2 - p0 * p1 - p0 * p2 - p1 * p2 + p0 * p1 * p2;

        } else if(links.length == 4) {
            double a = links[0].getWeight()* links[0].getDestinationNode().getPropagatedPoF(allTypes);
            double b = links[1].getWeight()* links[1].getDestinationNode().getPropagatedPoF(allTypes);
            double c = links[2].getWeight()* links[2].getDestinationNode().getPropagatedPoF(allTypes);
            double d = links[3].getWeight()* links[3].getDestinationNode().getPropagatedPoF(allTypes);

            return a+b+c+d
                    -a*b-a*c-a*d-b*c-b*d-c*d
                    +a*b*c+a*b*d+a*c*d+b*c*d
                    -a*b*c*d;
        } else throw new IllegalArgumentException("Links size must be 2 or 3");
    }

    @Override
    public Set<Type> getConnectedTypes() {
        Set<Type> t = new HashSet<Type>();
        for(Link l : links) {
            t.addAll(l.getDestinationNode().getConnectedTypes());
        }
        return t;
    }

    @Override
    public Object clone() {
        Link[] l = new Link[links.length];
        for(int i = 0 ; i < l.length ; i++) {
            l[i] = (Link)links[i].clone();
        }
        return new NodeUnion(l);
    }
}
