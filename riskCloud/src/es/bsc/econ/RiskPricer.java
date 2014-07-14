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
package es.bsc.econ;

import es.bsc.Constants;
import es.bsc.wl.Provider;
import es.bsc.wl.SLA;
import es.bsc.wl.Type;
import es.bsc.wl.WorkloadGenerator;

import static es.bsc.Constants.*;

public class RiskPricer implements Pricer {

    private Provider provider;

    public RiskPricer(Provider provider) {
        this.provider = provider;
    }

    @Override
    public void calculatePrice(SLA sla) {
        double reservationPrice = 0;
        for(Type t : sla.getHeadNode().getConnectedTypes()) {
            reservationPrice += t.getReservationPrice();
        }
        // calculates the market price in function of the status
        double reservationPriceBuyer = sla.getDuration() * sla.getResourceUnitsSum() * CLIENT_RP_COMPUTE_UNIT_HOUR;

        double aggressivenessFactor = provider.getAggressivenessFactor(sla.getStartTime(),sla.getDuration());

        double price = reservationPrice + aggressivenessFactor * (reservationPriceBuyer - reservationPrice);

//        System.out.println("bestPrice = " + bestPrice);
//        System.out.println("sla.getPenaltyMultiplicator() = " + sla.getPenaltyMultiplicator());
//        System.out.println("sla.getHeadNode().getPropagatedPoF() = " + sla.getHeadNode().getPropagatedPoF());

        sla.setPrice((price )//+ price * Constants.MP[sla.getPolicy().ordinal()] * sla.getHeadNode().getPropagatedPoF())
           * Constants.OVERPRICE[sla.getPolicy().ordinal()]
        );

    }


}
