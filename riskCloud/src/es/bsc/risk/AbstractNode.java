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

import java.util.Set;

public abstract class AbstractNode implements Cloneable {
    public double getPropagatedPoF() {
        // Does not count "alltypes" because do not have to consider them to add the overload (this is only performed
        // during the allocation process
        return getPropagatedPoF(null);
    }
    /**
     *
     * @param allTypes Is used to calculate the overload that would cause the allocation of a SLA in the resources pool
     * @return
     */
    public abstract double getPropagatedPoF(Set<Type> allTypes);
    public abstract Set<Type> getConnectedTypes();

    @Override public abstract Object clone();
}
