package rhp.aof4oop.framework.core;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import rhp.aof4oop.framework.core.exceptions.EFrameworkFault;


/**
 * Based on  http://www.fromdev.com/2008/09/building-faster-and-efficient-cache.html
 * @author rhp
 */
public class CCache extends AbstractMap<String, Object> 
{
  /** The internal HashMap that will hold the SoftReference. */
  private final Map<String,SoftValue> hash = new HashMap<String,SoftValue> ();
  /** Saves LOIDs by its memory object identity. Since System.identityHashCode() may return the same id for two distinct object a HashSet saves all LOID occurrences
   see JDK-6321873 : (spec) System.identityHashCode doc inadequate, Object.hashCode default implementation docs mislead
       JDK-6809470 : RFE: Change System.identityHashCode() implementation to return a unique value for distinct object **/
  
  /**
   * Maps all logical objects using its System.identityHashCode() as key. See note (a).
   * Key: persistentObject's System.identityHashCode() result
   * Entries: The LOID of the objects that share that same System.identityHashCode()
   * 
   *  Note a) The System.identityHashCode() function do not ensure that all instances have a distinct Id.
   *          Thus, all object that share a common ID are registered in the same key.
   *          Using the cache, it is possible to find the correct LOID
   */
  private final Map<Integer,HashSet<Long>> objectMap= new HashMap <Integer,HashSet<Long>>();

  /**
   * Maps all reachable objects using its System.identityHashCode() as key. See note (a).
   * Key: reachableObject's System.identityHashCode() result
   * Entries: The LOID of the near persistent objects that share that same System.identityHashCode() . See note (b)
   * 
   *  Note a) The System.identityHashCode() function do not ensure that all instances have a distinct Id.
   *          Thus, all object that share a common ID are registered in the same key.
   *          Using the cache, it is possible to find the correct LOID
   *  Note b) One object may be reachable from several persistent objects.
   *          Thus, several entries may exist.   
   */
  private final Map<Integer,HashSet<Long>> reachableObjects= new HashMap <Integer,HashSet<Long>>();					// key(object_id) :: Value(LOID of near persistent object)

  
  /** The number of "hard" references to hold internally. */
  private final int HARD_SIZE;
  /** The FIFO list of hard references, order of last access. */
  private final LinkedList<Object> hardCache = new LinkedList<Object>();
  /** Reference queue for cleared SoftReference objects. */
  private final ReferenceQueue queue = new ReferenceQueue();
  
  private int objectCacheHits;
  private int objectCacheAccess;
  private int objectCacheHitsPart;
  private int objectCacheAccessPart;

  public CCache() 
  {
	  this(400000); 
  }
  public CCache(int hardSize) 
  { 
	  HARD_SIZE = hardSize; 
	  objectCacheHits=0;
	  objectCacheAccess=0;
	  objectCacheHitsPart=0;
	  objectCacheAccessPart=0;
  }

  public Object get(Object key) 
  {
    Object result = null;
    // We get the SoftReference represented by that key
    SoftReference soft_ref = (SoftReference)hash.get(key);
    if (soft_ref != null) 
    {
      // From the SoftReference we get the value, which can be
      // null if it was not in the map, or it was removed in
      // the processQueue() method defined below
      result = soft_ref.get();
      if (result == null) 
      {
        // If the value has been garbage collected, remove the
        // entry from the HashMap.
        hash.remove(key);
      } 
      else 
      {
        // We now add this object to the beginning of the hard
        // reference queue.  One reference can occur more than
        // once, because lookups of the FIFO queue are slow, so
        // we don't want to search through it each time to remove
        // duplicates.
        hardCache.addFirst(result);
        if (hardCache.size() > HARD_SIZE) 
        {
          // Remove the last entry if list longer than HARD_SIZE
          hardCache.removeLast();
        }
      }
    }
    return result;
  }

  /** We define our own subclass of SoftReference which contains
   not only the value but also the key to make it easier to find
   the entry in the HashMap after it's been garbage collected. */
  private static class SoftValue extends SoftReference<Object>
  {
	  private final String key; // always make data member final
	  private final long LOID;
	  private final boolean emulated;	// If TRUE, this reference is emulated. That is, the object was converted from another version 
	  
	  /** Did you know that an outer class can access private data
     members and methods of an inner class?  I didn't know that!
     I thought it was only the inner class who could access the
     outer class's private information.  An outer class can also
     access private members of an inner class inside its inner
     class. */
	  private SoftValue(Object value,String key,long loid,boolean emutated,ReferenceQueue q) 
	  {
		  super(value, q);
		  this.key = key;
		  this.LOID=loid;
		  this.emulated=emutated;
	  }
	  public String getKey()
	  {
		  return key;
	  }
	  public long getLOID()
	  {
		  return LOID;
	  }
	public boolean isEmulated() {
		return emulated;
	}
	  
  }
  /**
   * Calculates the key for an object 
   * @param object
   * @return
   */
  public static String cacheKey(long loid)
  {
	  if(loid<=0)
	  {
		  throw new IllegalArgumentException("Invalid LOID");
	  }
	  //return (object!=null?Integer.toHexString(System.identityHashCode(object)):null);
	  return ""+loid;
  }
  /** Here we go through the ReferenceQueue and remove garbage
   collected SoftValue objects from the HashMap by looking them
   up using the SoftValue.key data member. */
  private void processQueue() 
  {
    SoftValue sv;
    while ((sv = (SoftValue)queue.poll()) != null) 
    {
      hash.remove(sv.key); // we can access private data!
    }
  }
  /** Check if the object is in the cache **/
  public boolean isCached(Object object)
  {
	  return findCachedLogicalObjectID(object)>0;
  }
  /** Check if the object is in the cache **/
  public boolean isCachedByLOID(long loid)
  {
	  return hash.get(cacheKey(loid))!=null;
  }
//  /** Here we put the key, value pair into the HashMap using
//   a SoftValue object. */
//  public Object put(String key, Object value,long loid) 
//  {
//    processQueue(); // throw out garbage collected values first
//    objectMap.put(new Long(System.identityHashCode(value)),new Long(loid));
//    return hash.put(key, new SoftValue(value, key,loid, queue));
//  }
  protected Object put(Object value,long loid,boolean emulated) 
  {
	String k=cacheKey(loid);
    processQueue(); // throw out garbage collected values first
    if(hash.size()>HARD_SIZE*0.9)
    {
    	debugMsg("Cache: "+(((double)hash.size()/(double)100))+"%");
    }
    registerPersistentObject(value,loid);
    return hash.put(k, new SoftValue(value, k,loid,emulated,queue));
  }
  /**
   * Register the objects
   * @param value - Logical Object
   * @param loid - Its LOID
   */
  private boolean registerPersistentObject(Object value,long loid)
  {
	    int obj_id=System.identityHashCode(value);
	    HashSet<Long> entry = objectMap.get(obj_id);
	    if(entry==null)
	    {
	    	//New entry
	    	entry=new HashSet<Long>();
	    	entry.add(loid);
	    	objectMap.put(obj_id,entry);
	    	return true; // was registered
	    }
	    else
	    {
	    	//Duplicate object identity
	    	if(!entry.contains(loid))
	    	{
	    		entry.add(loid);
	    		return true; // was registered
	    	}
	    	else
	    	{
	    		return false; // already registered
	    	}
	    }
  }
	/**
	 * Register memory_object as reachable from LOID 
	 * @param loid - LOID of the near persistent object
	 * @param memory_object
	 */
	private boolean registerReachableObject(long loid,Object memory_object)
	{
	    int obj_id=System.identityHashCode(memory_object);
	    HashSet<Long> entry = reachableObjects.get(obj_id);
	    if(entry==null)
	    {
	    	//New entry
	    	entry=new HashSet<Long>();
	    	entry.add(loid);
	    	reachableObjects.put(obj_id,entry);
	    	return true; // was registered
	    }
	    else
	    {
	    	//Duplicate object identity
	    	if(!entry.contains(loid))
	    	{
	    		entry.add(loid);	
		    	return true; // was registered
	    	}
	    	else
	    	{
	    		return false; // already registered
	    	}
	    }
	}
	/**
	 * Register all reachable objects from memory_object
	 * @param memory_object
	 */
	protected int registerReachableObjectTree(Object memory_object,long memory_object_loid)
	{
		try
		{
			return __registerReachabilityTree(memory_object,memory_object_loid,0);
		}
		catch(Exception e)
		{
			throw new EFrameworkFault(e.getMessage());
		}
	}
	/**
	 * Register all object's fields as reachable 
	 * @param root_obj
	 * @param loid
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private int __registerReachabilityTree(Object root_obj,long loid,int level) throws IllegalArgumentException, IllegalAccessException
	{
		int n=0;
		if(root_obj!=null)
		{
			boolean was_reg=registerReachableObject(loid,root_obj);
			if(was_reg)//Was registered, so entire subtree also needs to be reachable
			{
				n++;
				Field[] flds = CPersistentRoot.reflectFields(root_obj.getClass());
				for(Field f:flds)
				{
					if(!f.getName().startsWith("ajc$"))
					{
						f.setAccessible(true);
						Object o=f.get(root_obj);
						if(o!=null && !CPersistentRoot.isPrimitiveDataTypeObject(o.getClass().getCanonicalName()))
						{
							debugMsg("Level: "+level+" ::  "+root_obj.getClass().getCanonicalName()+" member "+f.getName()+"  from "+loid);						
							n+=__registerReachabilityTree(o,loid,level+1);
						}
					}
				}
			}			
		}
		return n;
	}
	/**
	 * Find the list of objects that reach memory_object
	 * @param memory_object
	 * @return
	 */
	protected ArrayList<Long> findRechability(Object memory_object)
	{
		ArrayList<Long> out=new ArrayList<Long>();
		long l=findCachedLogicalObjectID(memory_object);
		if(l>0)
		{
			// Is persistent, so is reachable
			out.add(l);
		}
	    int obj_id=System.identityHashCode(memory_object);
	    HashSet<Long> entry = reachableObjects.get(obj_id);
	    if(entry!=null)
	    {
	    	Iterator<Long> it = entry.iterator();
	    	while(it.hasNext())
	    	{
	    		//Check all occurrences with same object id 
	    		Long loid=it.next();
	    		Object root_obj=findCachedObjectByLOID(loid);
	    		int d=reachabilityDistance(root_obj,memory_object);
	    		if(d>=0)
	    		{
	    			out.add(loid);
	    		}
	    	}
	    }
	    return out;
	}
	/**
	 * Calculates the reachability distance from root_obj to obj
	 * @param root_obj
	 * @param obj
	 * @return 0: if they are the same object /  <0: if they don't are reachable / >0: if they are reachable 
	 */
	private int reachabilityDistance(Object root_obj,Object obj)
	{
		try
		{
			ArrayList<Object> already_checked=new ArrayList<Object>(); 
			return __reachabilityDistance(root_obj, obj,-1,already_checked);
		}
		catch(Exception e)
		{
			throw new EFrameworkFault(e.getMessage());
		}
	}
	private int __reachabilityDistance(Object root_obj,Object obj,int n,ArrayList<Object> already_checked) throws IllegalArgumentException, IllegalAccessException
	{
		if(root_obj!=null && obj!=null)
		{
			if(already_checked.contains(obj))
			{
				debugMsg("Circular reference!!!");
				return -1;
			}
			else
			{
				already_checked.add(obj);
				debugMsg("Checked: "+already_checked.size());
			}
			//if(root_obj.equals(obj))
			if(root_obj==obj)
			{
				return n+1;
			}
			Field[] flds = CPersistentRoot.reflectFields(root_obj.getClass());
			for(Field f:flds)
			{
				if(!f.getName().startsWith("ajc$"))
				{
					f.setAccessible(true);
					Object o=f.get(root_obj);
					if(o!=null && !CPersistentRoot.isPrimitiveDataTypeObject(o.getClass().getCanonicalName()))
					{
						int m=__reachabilityDistance(o, obj,n+1,already_checked);
						if(m>0)
						{
							return m;
						}
					}
				}
			}
		}
		return 0;
	}
  public Object removeByLOID(long loid) 
  {
    processQueue(); // throw out garbage collected values first
    
    String key=cacheKey(loid);
    Object obj=get(key);
    if(obj==null)
    {
    	return null;
    }
    int obj_id=System.identityHashCode(obj);
    HashSet<Long> entry = objectMap.get(obj_id);
    if(entry==null)
    {
    	throw new EFrameworkFault("Entry not found");
    }
    else
    {
    	int n_a=entry.size();
    	entry.remove(loid);
    	int n_d=entry.size();
    	debugMsg("Removing ("+obj_id+") LOID "+loid+"   entries "+n_a+" -> "+n_d);
    	return hash.remove(key);
    }
  }
  public void clear() 
  {
    hardCache.clear();
    processQueue(); // throw out garbage collected values
    hash.clear();
  }
  public int size() 
  {
    processQueue(); // throw out garbage collected values first
    return hash.size();
  }
  public Set entrySet() 
  {
    // no, no, you may NOT do that!!! GRRR
    throw new UnsupportedOperationException();
  }
  /**
   * Find a cached object by it LOID
   * @param logicalObjectId
   * @return
   */
	protected Object findCachedObjectByLOID(long logicalObjectId)
	{
		long tini=System.currentTimeMillis();
		SoftValue sv=hash.get(cacheKey(logicalObjectId));
		objectCacheAccess++;
		objectCacheAccessPart++;

		if(sv!=null)
		{
			objectCacheHits++;
			objectCacheHitsPart++;
			long tend=System.currentTimeMillis();
			CPersistentRoot.statsTimeCacheFindObject+=(tend-tini);
			return sv.get();
		}
		else
		{
			long tend=System.currentTimeMillis();
			CPersistentRoot.statsTimeCacheFindObject+=(tend-tini);
			return null;
		}
	}
	protected Object[] findCachedArrayByLOID(long logicalObjectId)
	{
		long tini=System.currentTimeMillis();
		SoftValue sv=hash.get(cacheKey(logicalObjectId));
		objectCacheAccess++;
		objectCacheAccessPart++;

		if(sv!=null)
		{
			objectCacheHits++;
			objectCacheHitsPart++;
			long tend=System.currentTimeMillis();
			CPersistentRoot.statsTimeCacheFindObject+=(tend-tini);
			return (Object[])sv.get();
		}
		else
		{
			long tend=System.currentTimeMillis();
			CPersistentRoot.statsTimeCacheFindObject+=(tend-tini);
			return null;
		}
	}
	/**
	 * Find the LOID of an instantiated object
	 * Notice that, just persistent objects have an associated LOID
	 * TODO: advice this method in order to obtain stats
	 * @param memory_object
	 * @return
	 */
	protected long findCachedLogicalObjectID(Object memory_object)
	{
		//This is very expensive in terms of performance
		long tini=System.currentTimeMillis();
		objectCacheAccess++;
		objectCacheAccessPart++;

		int obj_id=System.identityHashCode(memory_object);
	    HashSet<Long> entry = objectMap.get(obj_id);
	    if(entry==null)
	    {
			long tend=System.currentTimeMillis();
			CPersistentRoot.statsTimeCacheFindLOID+=(tend-tini);
	    	return 0;
	    }
	    else
	    {
	    	Iterator<Long> it = entry.iterator();
	    	while(it.hasNext())
	    	{
	    		//Check all occurrences with same object id 
	    		Long loid=it.next();
	    		SoftValue sv=hash.get(cacheKey(loid));
				if(sv.get()==memory_object)
				{
					//The cached object have this LOID
					objectCacheHits++;
					objectCacheHitsPart++;
					long tend=System.currentTimeMillis();
					CPersistentRoot.statsTimeCacheFindLOID+=(tend-tini);
					debugMsg("Find object with identityHashCode="+obj_id+" and loid="+sv.getLOID());
					return sv.getLOID();
				}
	    	}
	    	return 0;
	    }
	}
	/**
	 * Find the LOIDs of an instantiated array of objects
	 * Notice that, just persistent objects have an associated LOID
	 * TODO: advice this method in order to obtain stats
	 * @param memory_object
	 * @return
	 */
	protected long[] findCachedLogicalArrayID(Object[] memory_array)
	{
		long[] out=new long[memory_array.length];
		
		for(int i=0;i<memory_array.length;i++)
		{
			out[i]=findCachedLogicalObjectID(memory_array[i]);
		}
		return out;
	}
	protected boolean findCachedFlagEmulated(Object memory_object)
	{
		//This is very expensive in terms of performance
		long tini=System.currentTimeMillis();
		objectCacheAccess++;
		objectCacheAccessPart++;

		int obj_id=System.identityHashCode(memory_object);
	    HashSet<Long> entry = objectMap.get(obj_id);
	    if(entry==null)
	    {
			long tend=System.currentTimeMillis();
			CPersistentRoot.statsTimeCacheFindLOID+=(tend-tini);
	    	return false;
	    }
	    else
	    {
	    	Iterator<Long> it = entry.iterator();
	    	while(it.hasNext())
	    	{
	    		//Check all occurrences with same object id 
	    		Long loid=it.next();
	    		SoftValue sv=hash.get(cacheKey(loid));
				if(sv.get()==memory_object)
				{
					//The cached object have this LOID
					objectCacheHits++;
					objectCacheHitsPart++;
					long tend=System.currentTimeMillis();
					CPersistentRoot.statsTimeCacheFindLOID+=(tend-tini);
					debugMsg("Find "+obj_id+" with loid="+sv.getLOID());
					return sv.isEmulated();
				}
	    	}
	    	return false;
	    }
	}
	protected void dump()
	{
		System.out.println("---------------------------------------------------------");
		System.out.println("Dump cache:");

		System.out.println("Entries: "+hash.size());
		System.out.println("key\t\tLOID\t\tobject ref");
		for(SoftValue sv:hash.values())
		{
			System.out.print(sv.getKey()+"\t::\tLOID{"+sv.getLOID()+"}  ->  ");
			if(sv.get()!=null)
				System.out.println(sv.get().getClass().getName()+" = "+System.identityHashCode(sv.get()));
			else
				System.out.println("null");
		}
		System.out.println("---------------------------------------------------------");
	}
	public void resetObjectMapLap()
	{
		objectCacheAccessPart=0;
		objectCacheHitsPart=0;
	}
	public void showStatus()
	{
		System.out.println("Object cache Status:");
		System.out.println("Since start: objectCacheAccess="+objectCacheAccess+"   objectCacheHits="+objectCacheHits+"   hits="+(((double)objectCacheHits/(double)objectCacheAccess)*100)+"%");
		System.out.println("This lap: objectCacheAccessPart="+objectCacheAccessPart+"   objectCacheHitsPart="+objectCacheHitsPart+"   hits="+(((double)objectCacheHitsPart/(double)objectCacheAccessPart)*100)+"%");
	}
	/**
	 * This a utility that fills the cache to force to see which are the references that come out from cache.
	 */
	protected void fillWidthTrash()
	{
		for(int i=0;i<20;i++)
		{
			byte[] trash=new byte[1024*1024*64];
			put(trash,-1,false);
		}
	}
	private static void debugMsg(String msg)
	{
		System.out.println("[Cache]::"+msg);
	}
}