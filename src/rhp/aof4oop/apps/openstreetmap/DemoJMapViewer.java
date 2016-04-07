package rhp.aof4oop.apps.openstreetmap;

//TODO: calcular zoom
//TODO: Show nodes
//TODO: Show ways
//TODO: Show tags. e.g.: shopping center
//TODO: perceber porque razão o rectangulo azul não aparece após a importação de uma áre, é necessário reiniciar a aplicação

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.bind.JAXBException;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.JMapViewerTree;
import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.LayerGroup;
import org.openstreetmap.gui.jmapviewer.MapMarkerCircle;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.MapRectangleImpl;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOpenAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import rhp.aof4oop.framework.core.CPersistentRoot;
import rhp.aof4oop.framework.core.CQuery;
import rhp.osm.datamodel.xml.Member;
import rhp.osm.datamodel.xml.Node;
import rhp.osm.datamodel.xml.NodeReference;
import rhp.osm.datamodel.xml.OSMFile;
import rhp.osm.datamodel.xml.Relation;
import rhp.osm.datamodel.xml.Tag;
import rhp.osm.datamodel.xml.Way;
/**
*
* Demonstrates the usage of {@link JMapViewer}
*
* @author Jan Peter Stotz
*
* This app was modified by in order to use the {@link AOF4OOP} framework.
* Currently, the data model exists in three versions: A, B and C
* The versions A an B are the ones used in our case study published in ours papers. 
*/
public class DemoJMapViewer extends JFrame implements JMapViewerEventListener
{

	private static final long serialVersionUID = 1L;

	private JMapViewerTree treeMap = null;

	private JLabel zoomLabel=null;
	private JLabel zoomValue=null;

	private JLabel mperpLabelName=null;
	private JLabel mperpLabelValue = null;
	
	
	@SuppressWarnings("rawtypes")
	JComboBox comboAreaSelector = new JComboBox();
	
	private CPersistentRoot psRoot;
	private ArrayList<Area> loadedAreas;
	private Hashtable<String,LayerGroup> layerAreas;
	/**
	 * Constructs the {@code Demo}.
	 */
	public DemoJMapViewer() 
	{
		super("JMapViewer - powered by AOF4OOP");
		setSize(600, 400);

		treeMap = new JMapViewerTree("Zones");

		// Listen to the map viewer for user operations so components will
		// recieve events and update
		map().addJMVListener(this);

		// final JMapViewer map = new JMapViewer(new MemoryTileCache(),4);
		// map.setTileLoader(new OsmFileCacheTileLoader(map));
		// new DefaultMapController(map);

		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		JPanel panel = new JPanel();
		JPanel panelTop = new JPanel();
		JPanel panelBottom = new JPanel();
		JPanel helpPanel = new JPanel();
		
		
		JButton importButton=new JButton("Import File");
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				try
				{
					importFile();
				}
				catch(Exception ex)
				{
					
				}
			}
		});

		mperpLabelName=new JLabel("Meters/Pixels: ");
		mperpLabelValue=new JLabel(String.format("%s",map().getMeterPerPixel()));

		zoomLabel=new JLabel("Zoom: ");
		zoomValue=new JLabel(String.format("%s", map().getZoom()));

		add(panel, BorderLayout.NORTH);
		add(helpPanel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout());
		panel.add(panelTop, BorderLayout.NORTH);
		panel.add(panelBottom, BorderLayout.SOUTH);
		JLabel helpLabel = new JLabel("Use right mouse button to move,\n " + "left double click or mouse wheel to zoom.");
		helpPanel.add(helpLabel);
		JButton button = new JButton("setDisplayToFitMapMarkers");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				map().setDisplayToFitMapMarkers();
			}
		});

		JComboBox tileSourceSelector = new JComboBox(new TileSource[] { new OsmTileSource.Mapnik(),new OsmTileSource.CycleMap(), new BingAerialTileSource(), new MapQuestOsmTileSource(), new MapQuestOpenAerialTileSource() });
		tileSourceSelector.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				map().setTileSource((TileSource) e.getItem());
			}
		});
		@SuppressWarnings("rawtypes")
		JComboBox tileLoaderSelector;
		try 
		{
			tileLoaderSelector = new JComboBox(new TileLoader[] { new OsmFileCacheTileLoader(map()),new OsmTileLoader(map()) });
		} 
		catch (IOException e) 
		{
			tileLoaderSelector = new JComboBox(new TileLoader[] { new OsmTileLoader(map()) });
		}
		tileLoaderSelector.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				map().setTileLoader((TileLoader) e.getItem());
			}
		});
		map().setTileLoader((TileLoader) tileLoaderSelector.getSelectedItem());
		panelTop.add(tileSourceSelector);
		panelTop.add(tileLoaderSelector);
		final JCheckBox showMapMarker = new JCheckBox("Map markers visible");
		showMapMarker.setSelected(map().getMapMarkersVisible());
		showMapMarker.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				map().setMapMarkerVisible(showMapMarker.isSelected());
			}
		});
		panelBottom.add(showMapMarker);
		///
		final JCheckBox showTreeLayers = new JCheckBox("Tree Layers visible");
		showTreeLayers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				treeMap.setTreeVisible(showTreeLayers.isSelected());
			}
		});
		panelBottom.add(showTreeLayers);
		///
		final JCheckBox showToolTip = new JCheckBox("ToolTip visible");
		showToolTip.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				map().setToolTipText(null);
			}
		});
		panelBottom.add(showToolTip);
		///
		final JCheckBox showTileGrid = new JCheckBox("Tile grid visible");
		showTileGrid.setSelected(map().isTileGridVisible());
		showTileGrid.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				map().setTileGridVisible(showTileGrid.isSelected());
			}
		});
		panelBottom.add(showTileGrid);
		final JCheckBox showZoomControls = new JCheckBox("Show zoom controls");
		showZoomControls.setSelected(map().getZoomContolsVisible());
		showZoomControls.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				map().setZoomContolsVisible(showZoomControls.isSelected());
			}
		});
		panelBottom.add(showZoomControls);
		final JCheckBox scrollWrapEnabled = new JCheckBox("Scrollwrap enabled");
		scrollWrapEnabled.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				map().setScrollWrapEnabled(scrollWrapEnabled.isSelected());
			}
		});
		panelBottom.add(scrollWrapEnabled);
		panelBottom.add(button);

		panelTop.add(importButton);
		panelTop.add(zoomLabel);
		panelTop.add(zoomValue);
		panelTop.add(mperpLabelName);
		panelTop.add(mperpLabelValue);

		add(treeMap, BorderLayout.CENTER);



		//showCasa(treeMap);
		try
		{
			psRoot=new CPersistentRoot();
			psRoot.setGcVerbose(1);
			loadedAreas=new ArrayList<Area>();
			layerAreas=new Hashtable<String, LayerGroup>();
	
			//showGermany(treeMap);
			showDB(panelTop);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,e.toString(),"Exception",JOptionPane.ERROR_MESSAGE);
		}

		//	         map().setDisplayPositionByLatLon(49.807, 8.6, 11);
		//	         map().setTileGridVisible(true);

		map().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					map().getAttribution().handleAttribution(e.getPoint(), true);
				}
			}
		});

		map().addMouseMotionListener(new MouseAdapter() 
		{
			@Override
			public void mouseMoved(MouseEvent e) 
			{
				Point p = e.getPoint();
				boolean cursorHand = map().getAttribution().handleAttributionCursor(p);
				if (cursorHand) {
					map().setCursor(new Cursor(Cursor.HAND_CURSOR));
				} else {
					map().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				if(showToolTip.isSelected()) map().setToolTipText(map().getPosition(p).toString());
			}
		});
	}
	private JMapViewer map()
	{
		return treeMap.getViewer();
	}
	private static Coordinate c(double lat, double lon)
	{
		return new Coordinate(lat, lon);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// java.util.Properties systemProperties = System.getProperties();
		// systemProperties.setProperty("http.proxyHost", "localhost");
		// systemProperties.setProperty("http.proxyPort", "8008");
		new DemoJMapViewer().setVisible(true);
	}

	private void updateZoomParameters() 
	{
		if (mperpLabelValue!=null)
		{
			mperpLabelValue.setText(String.format("%s",map().getMeterPerPixel()));
		}
		if (zoomValue!=null)
		{
			zoomValue.setText(String.format("%s", map().getZoom()));
		}
	}

	//com esta anotacao não compila com o ant @Override
	public void processCommand(JMVCommandEvent command) 
	{
		if (command.getCommand().equals(JMVCommandEvent.COMMAND.ZOOM) || command.getCommand().equals(JMVCommandEvent.COMMAND.MOVE)) 
		{
			updateZoomParameters();
		}
	}


	public static void showGermany(JMapViewerTree treeMap)
	{
		JMapViewer map = treeMap.getViewer();
		LayerGroup germanyGroup = new LayerGroup("Germany");
		Layer germanyWestLayer = germanyGroup.addLayer("Germany West");
		Layer germanyEastLayer = germanyGroup.addLayer("Germany East");
		MapMarkerDot eberstadt = new MapMarkerDot(germanyEastLayer, "Eberstadt", 49.814284999, 8.642065999);
		MapMarkerDot ebersheim = new MapMarkerDot(germanyWestLayer, "Ebersheim", 49.91, 8.24);
		MapMarkerDot empty = new MapMarkerDot(germanyEastLayer, 49.71, 8.64);
		MapMarkerDot darmstadt = new MapMarkerDot(germanyEastLayer, "Darmstadt", 49.8588, 8.643);
		map.addMapMarker(eberstadt);
		map.addMapMarker(ebersheim);
		map.addMapMarker(empty);
		Layer franceLayer = treeMap.addLayer("France");
		map.addMapMarker(new MapMarkerDot(franceLayer, "La Gallerie", 48.71, -1));
		map.addMapMarker(new MapMarkerDot(43.604, 1.444));
		map.addMapMarker(new MapMarkerCircle(53.343, -6.267, 0.666));
		map.addMapRectangle(new MapRectangleImpl(new Coordinate(53.343, -6.267), new Coordinate(43.604, 1.444)));
		map.addMapMarker(darmstadt);
		treeMap.addLayer(germanyWestLayer);
		treeMap.addLayer(germanyEastLayer);

		MapPolygon bermudas = new MapPolygonImpl(c(49,1), c(45,10), c(40,5));
		map.addMapPolygon( bermudas );
		map.addMapPolygon( new MapPolygonImpl(germanyEastLayer, "Riedstadt", ebersheim, darmstadt, eberstadt, empty));


		map.addMapMarker(new MapMarkerCircle(germanyWestLayer, "North of Suisse", new Coordinate(48, 7), .5));
	}

	public static void showSpain(JMapViewerTree treeMap)
	{
		JMapViewer map = treeMap.getViewer();


		Layer wales = treeMap.addLayer("UK");
		map.addMapRectangle(new MapRectangleImpl(wales, "Wales", c(53.35,-4.57), c(51.64,-2.63)));
	}

	public static void showWales(JMapViewerTree treeMap)
	{
		JMapViewer map = treeMap.getViewer();

		Layer wales = treeMap.addLayer("UK");
		map.addMapRectangle(new MapRectangleImpl(wales, "Wales", c(53.35,-4.57), c(51.64,-2.63)));
	}


	
	public static void showFile(JMapViewerTree treeMap) throws JAXBException
	{
		OSMFile osm = OSMFile.load("zona_casa.osm");
		showFile(treeMap, osm,"Zona da casa do RHP");
	}
	
	public static void showFile(JMapViewerTree treeMap,OSMFile osm,String title)
	{
		JMapViewer map = treeMap.getViewer();


		Layer zona = treeMap.addLayer(osm.getBounds().toString());

		//double aa=41.189;
		//double bb=-8.6401;

		//double cc=41.1819;
		//double dd=-8.6317;

		double aa=osm.getBounds().getMaxlat();
		double bb=osm.getBounds().getMinlon();

		double cc=osm.getBounds().getMinlat();
		double dd=osm.getBounds().getMaxlon();

		map.addMapRectangle(new MapRectangleImpl(zona,title, c(aa,bb), c(cc,dd)));
		//map.addMapRectangle(new MapRectangleImpl(zona, "RHP Zone", c(41.189,-8.6401), c(41.1819,-8.6317)));

		//		  MinLat: 41.1766000
		//		  MaxLat: 41.1935000
		//		  MinLon: -8.6497000
		//		  MaxLon: -8.6265000

		for(int i=0;i<osm.getNodes().length;i++)
		{
			Node n = osm.getNodes()[i];
			MapMarkerDot nodo = new MapMarkerDot(zona, ""+n.getId(),n.getLat(),n.getLon());
			map.addMapMarker(nodo);
		}

		//
		map.setDisplayPositionByLatLon(41.189,-8.6401, 16);
		//	      map().setTileGridVisible(true);

	}
	public void importFile() throws JAXBException
	{
		final JFileChooser fc = new JFileChooser();

		 int returnVal = fc.showOpenDialog(this);

	     if (returnVal == JFileChooser.APPROVE_OPTION) 
	     {
	            File file = fc.getSelectedFile();
	            //This is where a real application would open the file.
	            System.out.println("Opening: " + file.getAbsolutePath());
	            
	    		int r=JOptionPane.showConfirmDialog(this,"Confirma a importação do ficheiro "+file.getName()+"?");
	    		if(r==0)
	    		{
	    			String name=JOptionPane.showInputDialog(this,"Area name");
	    			if(name==null || name.length()<2)
	    			{
	    				JOptionPane.showMessageDialog(this,"The name of the area is not a valid one!!!");
	    			}
	    			else
	    			{
	    				Area importedArea=importFile(name,file.getAbsolutePath());
	    				//Work with primitives to avoid Get Aspect
	    				int t_nodes_imported=importedArea.getNodes().length;
	    				int s_nodes=(t_nodes_imported/20);
	    				s_nodes=(s_nodes<=0?10:s_nodes);
						long[] nos_importados=new long[importedArea.getNodes().length];
						for(int i=0;i<importedArea.getNodes().length;i++)
						{
							nos_importados[i]=importedArea.getNodes()[i].getId();
						}

						int t_ways_imported=0;
						int s_ways=0;
						long[] ways_importados=null;
						if(importedArea.getWays()!=null)
						{
							t_ways_imported=importedArea.getWays().length;
		    				s_ways=(importedArea.getWays().length/20);
		    				s_ways=(s_ways<=0?10:s_ways);
							ways_importados=new long[importedArea.getWays().length];
							for(int i=0;i<t_ways_imported;i++)
							{
								ways_importados[i]=importedArea.getWays()[i].getId();
							}
						}
	    				
	    				System.out.println("Imported area "+importedArea.getName()+" with "+nos_importados.length+" nodes  and "+(importedArea.getWays()!=null?importedArea.getWays().length:0)+" ways");
	    				for(Area a:loadedAreas)
	    				{
    						long[] nos_area_id=new long[a.getNodes().length];
    						Node[] nos_area_ob=new Node[a.getNodes().length];
    						for(int i=0;i<a.getNodes().length;i++)
    						{
    							nos_area_id[i]=a.getNodes()[i].getId();
    							nos_area_ob[i]=a.getNodes()[i];
    						}

    						
	    					System.out.print("Find shared nodes with "+a.getName()+":\t\t\t");
	    					int comuns=0;
    						for(int i=0;i<t_nodes_imported;i++)
    						{
    							if(i%s_nodes==0)
    							{
    								System.out.print(".");
    							}
    							long new_n=nos_importados[i];
    							for(int j=0;j<nos_area_id.length;j++)
    							{
	    							if(nos_area_id[j]==new_n)
	    							{
	    								importedArea.getNodes()[i]=nos_area_ob[j];
	    								comuns++;
	    							}
	    						}
	    					}
    						System.out.println("* "+a.getNodes().length+" nodes, which "+comuns+" are shared");
    						
    						
    						Way[] a_ways=a.getWays();
    						if(t_ways_imported>0 && a_ways!=null)
    						{
        						long[] ways_area_id=new long[a.getWays().length];
        						for(int i=0;i<a.getWays().length;i++)
        						{
        							ways_area_id[i]=a.getWays()[i].getId();
        						}
        						
    							System.out.print("Find shared ways with "+a.getName()+":\t\t\t");
	    						comuns=0;
	    						for(int i=0;i<t_ways_imported;i++)
	    						{
	    							if(i%s_ways==0)
	    							{
	    								System.out.print(".");
	    							}
	    							long new_w_id=ways_importados[i];
	    							
	    							for(int j=0;j<a_ways.length;j++)
	    							{
		    							if(ways_area_id[j]==new_w_id)
		    							{
		    								importedArea.getWays()[i]=a_ways[j];
		    								comuns++;
		    							}
		    						}
		    					}
	    						System.out.println("* "+a.getWays().length+" ways, which "+comuns+" are shared");
    						}
	    				}
	    				System.out.println("Saving "+importedArea.calcKey()+"...");
	    				psRoot.setRootObject(importedArea.calcKey(),importedArea);
	    				System.out.println("Finished saving");
	    				
	    				loadedAreas.add(importedArea);
	    				drawArea(importedArea);
	    				showPosition(importedArea);
	    				comboAreaSelector.addItem(importedArea);
	    				JOptionPane.showMessageDialog(this,"Foram importados "+importedArea.getNodes().length+" nodes");
	    				
	    				CPersistentRoot.printStats();
	    			}
	    		}
	     } 
	     else 
	     {
	    	 System.out.println("Open command cancelled by user.");
	     }
	}
	public Area importFile(String name,String osm_filename) throws JAXBException
	{
			OSMFile osm = OSMFile.load(osm_filename);
			return importFile(name,osm);
	}
	
	public Area importFile(String name,OSMFile osm)
	{
		Area importedArea=new Area(name,osm.getBounds().getMinlat(),osm.getBounds().getMinlon(),osm.getBounds().getMaxlat(),osm.getBounds().getMaxlon(),osm.getNodes(),osm.getWays(),osm.getRelations());

		return importedArea;
	}
	public void showDB(JPanel panelTop)
	{
		JMapViewer map = map();

		

		//Count Nodes
//		System.out.println("Quering...");
//		List<Area> tmp=psRoot.query(new CQuery(Node.class));
//		System.out.println("Result: "+tmp.size()+" nodes");
		
		
		System.out.println("Quering...");
		List<Area> areas=psRoot.query(new CQuery(Area.class));
		System.out.println("Result: "+areas.size()+" areas");

		comboAreaSelector.addItem("Select an area");
		Area selectedArea=null;
		for(Area a:areas)
		{
			comboAreaSelector.addItem(a);
			selectedArea=a;
			loadedAreas.add(a);
			//System.out.println(" * Area: "+a.getName()+" nodes:"+a.getNodes().length+"  "+((System.currentTimeMillis()-t_ini)/1000)+" segs");
			//System.out.println(" * Area: "+a.getName()+" bounds:"+(a.bounds!=null?""+a.bounds.length:"NULO"));
		}
		CPersistentRoot.printStats();
		
		comboAreaSelector.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) 
			{
				if(e.getStateChange()==ItemEvent.SELECTED)
				{
					Object itm=e.getItem();
					if(itm instanceof Area)
					{
						Area a = (Area) itm;
						System.out.println("Selected Area "+a);
						showPosition(a);
						drawArea(a);
						CPersistentRoot.printStats();
					}
				}
				else if(e.getStateChange()==ItemEvent.DESELECTED)
				{
					Object itm=e.getItem();
					if(itm instanceof Area)
					{
						Area a = (Area)itm; 
						System.out.println("Deselected Area "+a);
					}
				}
			}
		});
		panelTop.add(comboAreaSelector);
		
//		System.out.println("Quering...");
//		List<Node> nodes=psRoot.query(new CQuery(Node.class));
//		System.out.println("Result: "+nodes.size()+" nodes");
//		for(Node n:nodes)
//		{
//			MapMarkerDot nodo = new MapMarkerDot(zona, ""+n.getId(),n.getLat(),n.getLon());
//			map.addMapMarker(nodo);
//		}
		

		if(selectedArea!=null)
		{
			showPosition(selectedArea);
		}
		else
		{
			map.setDisplayPositionByLatLon(41.189,-8.6401, 16);
		}
		//	      map().setTileGridVisible(true);

	}
	/**
	 * Show the map position of an Area
	 * @param a
	 */
	public void showPosition(Area a)
	{
		float zoom;
		float lat;
		float lon;
//Version A		
//		if(a.getMaxlat()>a.getMinlat())
//		{
//			zoom=(a.getMaxlat()-a.getMinlat());
//			lat=(a.getMaxlat()+a.getMinlat())/2;
//		}
//		else
//		{
//			zoom=(a.getMinlat()-a.getMaxlat());
//			lat=(a.getMinlat()+a.getMaxlat())/2;
//		}
//		
//		if(a.getMaxlon()>a.getMinlon())
//		{
//			lon=(a.getMaxlon()+a.getMinlon())/2;
//		}
//		else
//		{
//			lon=(a.getMinlon()+a.getMaxlon())/2;
//		}
//--------------------------------------------------		
// Version B
//		lat=0;
//		lon=0;
//		for(rhp.aof4oop.apps.openstreetmap.Coordinate c:a.getBounds())
//		{
//			lat+=c.getLat();
//			lon+=c.getLon();
//		}
//		lat=lat/a.getBounds().length;
//		lon=lon/a.getBounds().length;
		//--------------------------------------------------		
// Version C
				lat=0;
				lon=0;
				for(Coordinate c:a.getBounds())
				{
					lat+=c.getLat();
					lon+=c.getLon();
				}
				lat=lat/a.getBounds().length;
				lon=lon/a.getBounds().length;
//--------------------------------------------------		
		zoom=16;
		System.out.println("Zoom: "+zoom);
		//System.out.println("Area: "+a.getName()+" bounds:"+(a.bounds!=null?""+a.bounds.length:"NULO"));
		
		map().setDisplayPositionByLatLon(lat,lon, (int)(zoom));
	}
	/**
	 * Draw a layer.
	 * If it already exists returns its reference
	 * @param a
	 * @return
	 */
	private LayerGroup drawArea(Area a)
	{
//System.out.println("AAAAA "+System.identityHashCode(a)+"   LOID="+CPersistentRoot.findCachedLogicalObjectID(a));		
//a.setName(a.getName());		
//System.out.println("DDDDD "+System.identityHashCode(a)+"   LOID="+CPersistentRoot.findCachedLogicalObjectID(a));

//		System.out.println("Area="+a.getName());
//		System.out.println("Persistent a bounds="+CPersistentRoot.isPersistent(a)+"   "+CPersistentRoot.isPersistent(a.bounds));
//		System.out.println("Reachable a bounds="+CPersistentRoot.isReachable(a)+"   "+CPersistentRoot.isReachable(a.bounds));
		LayerGroup areaGroup=layerAreas.get(a.toString());
		if(areaGroup!=null)
		{
			return areaGroup;
		}
		areaGroup = new LayerGroup(a.getName());
		layerAreas.put(a.getName(),areaGroup);
		
		Layer l_zone = areaGroup.addLayer("Area");
		treeMap.addLayer(l_zone);			// Rectangle
		
		LayerGroup amenityGroup = new LayerGroup("Amenities");
		areaGroup.add(amenityGroup);
		
//		LayerGroup waysGroup = new LayerGroup("Ways");
//		areaGroup.add(amenityGroup);
		
		
		// Area
		//Version A
//		map().addMapPolygon(new MapPolygonImpl(l_zone,a.toString(),c(a.getMaxlat(),a.getMinlon()), c(a.getMaxlat(),a.getMaxlon()), c(a.getMinlat(),a.getMaxlon()), c(a.getMinlat(),a.getMinlon())));
		//Version B C
		map().addMapPolygon(new MapPolygonImpl(l_zone,a.toString(),a.getBounds()));
		
		// Show Amenities
		Color area_border_color=new Color(0,0,0);
		Color area_bckg_color=new Color(255,255,0);
		
		Hashtable<String,Layer> l_amenities=new Hashtable<String,Layer>();
		for(Node n:a.getNodes())
		{
			if(n.getTag()!=null)
			{
				String name=null;
				Layer l=null;
					for(Tag tag:n.getTag())
					{
						String k=tag.getKey();
						if(k==null)
						{

						}
						else if("amenity".equals(k))
						{
							String v=tag.getValue();
							//marketplace,shop... are amenity. So just when type sub-type of amenity is unknown it is consired
							if(v==null)
							{
								System.out.println("k="+k+"   v=NULL");
							}
							else 
							{
								l=l_amenities.get(v);
								if(l==null)
								{
									l=amenityGroup.addLayer(v);
									treeMap.addLayer(l);
									l_amenities.put(l.getName(),l);
									l.setVisible(false);
									//l.setVisibleTexts(true);
								}
								if(name==null)
								{
									name="no name "+v;
								}
							}
						}
						else if("name".equals(k))
						{
							name=tag.getValue();
						}
						else
						{
							//System.out.println("k="+k+"!!!");
						}
					}
					if(name!=null && l!=null)
					{
						MapMarkerDot nodo = new MapMarkerDot(l,name,n.getLat(),n.getLon());
						nodo.setBackColor(area_bckg_color);
						nodo.setColor(area_border_color);
						map().addMapMarker(nodo);
					}
				}
		}
		System.out.println("Indexing Nodes...");
		Hashtable<Long,Node> nodes_idx=new Hashtable<Long,Node>();
		for(Node n:a.getNodes())
		{
			nodes_idx.put(n.getId(),n);
		}
		
		System.out.println("Indexing Ways...");
		Hashtable<Long,Way> ways_idx=new Hashtable<Long,Way>();
		for(Way w:a.getWays())
		{
			ways_idx.put(w.getId(),w);
		}
		
		System.out.println("Presenting "+a.getWays().length+" Ways");
		for(Way w:a.getWays())
		{
			//System.out.println("Presenting Way "+w.getId()+"  tags="+(w.getTags()!=null?""+w.getTags().length:"no tags!!!"));
			if(w.getTags()!=null && w.getNodesRefs()!=null && w.getNodesRefs().length>0)
			{
				String name=null;
				Layer l=null;
				for(Tag tag:w.getTags())
				{
					String k=tag.getKey();
					if(k==null)
					{

					}
					else if("amenity".equals(k))
					{
						String v=tag.getValue();
						//marketplace,shop... are amenity. So just when type sub-type of amenity is unknown it is consired
						if(v==null)
						{
							System.out.println("k="+k+"   v=NULL");
						}
						else 
						{
							l=l_amenities.get(v);
							if(l==null)
							{
								l=amenityGroup.addLayer(v);
								treeMap.addLayer(l);
								l_amenities.put(l.getName(),l);
								l.setVisible(false);
								//l.setVisibleTexts(true);
							}
							if(name==null)
							{
								name="no name "+v;
							}
						}
					}
					else if("name".equals(k))
					{
						name=tag.getValue();
					}
					else
					{
						//System.out.println("k="+k+"!!!");
					}
					if(name!=null && l!=null)
					{
						ArrayList<Coordinate> coords=new ArrayList<Coordinate>();
						for(NodeReference nd:w.getNodesRefs())
						{
							Node n=nodes_idx.get(nd.getRef());
							coords.add(new Coordinate(n.getLat(),n.getLon()));
						}
						MapPolygonImpl p = new MapPolygonImpl(l,name,coords);
						p.setBackColor(area_bckg_color);
						p.setColor(area_border_color);
						map().addMapPolygon(p);
					}
				}
			}
		}
		if(a.getRelations()!=null)
		{
			System.out.println("Presenting "+a.getRelations().length+" Relations");
//			Layer l_boundary = areaGroup.addLayer("Boundary");
//			treeMap.addLayer(l_boundary);			// Boundary
			
			LayerGroup boundaryGroup = new LayerGroup("Boundary");
			areaGroup.add(boundaryGroup);
			for(Relation r:a.getRelations())
			{
				if(r.getTag()!=null)
				{
					for(Tag tag:r.getTag())
					{
						String k=tag.getKey();
						if(k==null)
						{
		
						}
						else if("type".equals(k))
						{
							if("boundary".equals(tag.getValue()))
							{
								Layer l=boundaryGroup.addLayer(findTagName(r.getTag())!=null?findTagName(r.getTag()):"no name");
								treeMap.addLayer(l);
								for(Member m:r.getMember())
								{
									if("way".equals(m.getType()) && "outer".equals(m.getRole()))
									{
										Way w = ways_idx.get(m.getRef());
										if(w!=null)
										{
											drawAsLine(l,w,nodes_idx,area_border_color);
										}
										else
										{
											System.err.println("ERROR("+findTagName(r.getTag())+"): invalid member "+m.getRef());
										}
									}
								}
							}
						}
						else
						{
							
						}
					}
				}
			}
		}
		return areaGroup;
	}
	
	public ArrayList<Coordinate> toCoordinates(Way w,Hashtable<Long,Node> nodes_idx)
	{
		ArrayList<Coordinate> coords=new ArrayList<Coordinate>();
		for(NodeReference nd:w.getNodesRefs())
		{
			Node n=nodes_idx.get(nd.getRef());
			coords.add(new Coordinate(n.getLat(),n.getLon()));
		}
		return coords;
	}
	public void drawAsLine(Layer layer,Way w,Hashtable<Long,Node> nodes_idx,Color area_border_color)
	{
		Node last=null;
		ArrayList<Coordinate> coords=null;
		for(NodeReference nd:w.getNodesRefs())
		{
			Node n=nodes_idx.get(nd.getRef());
			if(last!=null)
			{
				coords=new ArrayList<Coordinate>();
				coords.add(new Coordinate(last.getLat(),last.getLon()));
				coords.add(new Coordinate(n.getLat(),n.getLon()));
				coords.add(new Coordinate(n.getLat(),n.getLon()));
				MapPolygonImpl p = new MapPolygonImpl(layer,coords);
				p.setColor(area_border_color);
				p.setBackColor(area_border_color);
				map().addMapPolygon(p);
			}
			last=n;
		}
	}
	public String findTagName(Tag[] tags)
	{
		if(tags!=null)
		{
			for(Tag t:tags)
			{
				if("name".equals(t.getKey()))
				{
					return ""+t.getValue();
				}
			}
		}
		return null;
	}
}
