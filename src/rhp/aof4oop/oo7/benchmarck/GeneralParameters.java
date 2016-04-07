package rhp.aof4oop.oo7.benchmarck;

/* GeneralParameters.java
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


/**
* @author pvz 24-Apr-2006
* 
* 
* parameters for generating design information fields
* 
* First parameters for dates. Each type of object has a distinct date range, to
* make it easier to control the results of queries that compare dates.
* Currently the important relationship is between assembly objects and
* composite parts, since queries #5 and #6 ask for assemblies that use
* composite parts with build dates later than the date for the assembly.
* 
* The overall picture is that composite parts are divided into two classes,
* "old" and "young", such that we have the following picture:
* 
* ->---------old-------|---assembly----|------young------->
* 
* 
* 
* "Old" composite parts have build dates in this range.
* 
* assembly object have build dates in this range.
* 
* "young" composite parts have build dates in this range.
* 
* 
* 
* The constant "YoungCompFrac" determines the fraction of composite parts that
* are "young" --- about 1(div)YoungCompFrac of the composite parts are young.
* ("About" due to randomness in how the young parts are chosen.)
* 
*/
public class GeneralParameters {

	public static int minModuleDate = 1000; // lower bound for module

	// buildDate values

	public static int maxModuleDate = 1999; // upper bound for module

	// buildDate values

	public static int minAssmDate = 1000; // lower bound for assembly

	// buildDate values

	public static int maxAssmDate = 1999; // upper bound for assembly

	// buildDate values

	public static int minAtomicDate = 1000; // lower bound for atomic part

	// buildDate values

	public static int maxAtomicDate = 1999; // upper bound for atomic part

	// buildDate values

	public static int minOldCompDate = 0; // lower bound for "old" composite

	// part buildDate values

	public static int maxOldCompDate = 999; // upper bound for "old" composite

	// part buildDate values

	public static int minYoungCompDate = 2000; // lower bound for "young"
												// composite

	// part buildDate values

	public static int maxYoungCompDate = 2999; // upper bound for "young"
												// composite

	// part buildDate values

	public static int youngCompFrac = 10; // 1/YoungCompFrac composite parts

	// have "young" build dates (for
	// queries #5 and #6.)

	public static int numTypes = 10; // # different design type names

	// parameters for generating AtomicParts and Connections

	public static int xyRange = 100000; // number of x or y values

	// parameters for generating CompositeParts and Documents

	public static String documentText = "I am the documentation for composite part: ";

	public static int docTextLength = 80; // must be as long as the expanded

	// DocumentText

	public static String manualText = "I am the manual for module: ";

	public static int manualTextLength = 80; // must be as long as the
												// expanded

	// DocumentText

	public static int bytesPerCompositePart = 15000;//TODO:NOTE pvz: this are not used?!!!!!!
	
	public static int titleSize	= 40;

}
