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

public class NodeIntersection extends AbstractNode {
    private Link[] links = null;

    public NodeIntersection(Link[] links) {
        if(links == null || links.length < 2) {
            throw new IllegalArgumentException("Links size must be > 1");
        }
        this.links = links;
    }

    @Override
    public double getPropagatedPoF(Set<Type> allTypes) {
        double pof = 1;
        for(Link l : links) {
            pof *= l.getWeight() * l.getDestinationNode().getPropagatedPoF(allTypes);
        }
        return pof;
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
        return new NodeIntersection(l);
    }
}
