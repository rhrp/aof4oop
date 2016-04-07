/* BAidList.java
*
* Copyright (C) 2009 Pieter van Zyl
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
* 
*/
package rhp.aof4oop.oo7.benchmarck;
import java.util.Set;
import java.util.TreeSet;

/**
* Original OO7 comment:
* 
* structure for keeping track of a list of base assemblies that reference a
* composite part as either a shared or private member
* 
* Some implementations store the BaseAssembly id's to be linked to
* compositeParts later: Ontos implementation 
* 
* Others like the original oo7
* versant version stores the basesAssembly objects
* 
* This implementations stores the BaseAssembly id's.
* 
* The lookup for the matching BaseAssembly object using the id is done
* in the CompositePart with the linking.
* 
* It could be implemented here as in the Ontos version
* 
* @author pvz 23 Nov 2007
* 
*/
public class BAidList {

	private Set<Long> baIdList = null;

	/**
	 * pvz 10 Nov 2007
	 * 
	 * @return the baIdList
	 */
	public Set<Long> getBaIdList() {

		return baIdList;
	}

	/**
	 * pvz 10 Nov 2007
	 * 
	 * @param baIdList
	 *            the baIdList to set
	 */
	public void setBaIdList(Set<Long> baIdList) {
		this.baIdList = baIdList;
	}

	public Set<Long> next() {

		Set<Long> set = new TreeSet<Long>();
		set.add(baIdList.iterator().next());
		return set;
	}
	public int size()
	{
		return baIdList.size();
	}
}
