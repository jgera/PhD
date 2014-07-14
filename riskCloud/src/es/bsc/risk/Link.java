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

public class Link implements Cloneable {
    private double weight = 1;
    private AbstractNode destinationNode;

    public Link(AbstractNode destinationNode) {
        this.destinationNode = destinationNode;
    }

    public Link(double weight, AbstractNode destinationNode) {
        this(destinationNode);
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public AbstractNode getDestinationNode() {
        return destinationNode;
    }

    @Override
    public Object clone() {
        return new Link(weight,(AbstractNode)destinationNode.clone());
    }

}
