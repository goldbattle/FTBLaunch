/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2013, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
 * FTB Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ftb.gui.panes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.ftb.data.ModPack;
import net.ftb.data.Settings;
import net.ftb.data.events.ModPackListener;
import net.ftb.gui.LaunchFrame;
import net.ftb.gui.dialogs.EditModPackDialog;
import net.ftb.gui.dialogs.SearchDialog;
import net.ftb.locale.I18N;
import net.ftb.log.Logger;
import net.ftb.util.OSUtils;


public class ModpacksPane extends JPanel implements ILauncherPane, ModPackListener {
	private static final long serialVersionUID = 1L;

	private static JPanel packs;
	public static ArrayList<JPanel> packPanels;
	private static JScrollPane packsScroll;

	private JButton editModPack;

	private static JComboBox version;
	private static int selectedPack = 0;
	private static boolean modPacksAdded = false;
	private static HashMap<Integer, ModPack> currentPacks = new HashMap<Integer, ModPack>();
	private final ModpacksPane instance = this;
	private static JEditorPane packInfo;


	//	private JLabel loadingImage;
	public static String origin = "All", mcVersion = "All", avaliability = "All";
	public static boolean loaded = false;

	private static JScrollPane infoScroll;

	public ModpacksPane () {
		super();
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(null);

		packPanels = new ArrayList<JPanel>();

		packs = new JPanel();
		packs.setLayout(null);
		packs.setOpaque(false);

		// stub for a real wait message
		final JPanel p = new JPanel();
		p.setBounds(0, 0, 420, 55);
		p.setLayout(null);

		editModPack = new JButton(I18N.getLocaleString("MODS_EDIT_PACK"));
		editModPack.setBounds(5, 5, 110, 25);
		editModPack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(packPanels.size() > 0) {
					if(getSelectedModIndex() >= 0) {
						EditModPackDialog empd = new EditModPackDialog(LaunchFrame.getInstance());
						empd.setVisible(true);
					}
				}
			}
		});
		add(editModPack);

		JTextArea filler = new JTextArea(I18N.getLocaleString("MODS_WAIT_WHILE_LOADING"));
		filler.setBorder(null);
		filler.setEditable(false);
		filler.setForeground(Color.white);
		filler.setBounds(58, 6, 378, 42);
		filler.setBackground(new Color(255, 255, 255, 0));
		//		p.add(loadingImage);
		p.add(filler);
		packs.add(p);

		packsScroll = new JScrollPane();
		packsScroll.setBounds(-3, 30, 420, 283);
		packsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		packsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		packsScroll.setWheelScrollingEnabled(true);
		packsScroll.setOpaque(false);
		packsScroll.setViewportView(packs);
		packsScroll.getVerticalScrollBar().setUnitIncrement(19);
		add(packsScroll);

		packInfo = new JEditorPane();
		packInfo.setEditable(false);
		packInfo.setContentType("text/html");
		packInfo.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent event) {
				if(event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					OSUtils.browse(event.getURL().toString());
				}
			}
		});
		// TODO: Fix darker background for text area? Or is it better blending in?
		packInfo.setBackground(UIManager.getColor("control").darker().darker());
		add(packInfo);

		infoScroll = new JScrollPane();
		infoScroll.setBounds(410, 0, 430, 315);
		infoScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		infoScroll.setWheelScrollingEnabled(true);
		infoScroll.setViewportView(packInfo);
		infoScroll.setOpaque(false);
		add(infoScroll);

		version = new JComboBox(new String[]{});
		version.setBounds(115, 5, 130, 25);
		version.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Settings.getSettings().setPackVer((String.valueOf(version.getSelectedItem()).equalsIgnoreCase("recommended") ? "Recommended Version" : String.valueOf(version.getSelectedItem())));
				Settings.getSettings().save();
			}
		});
		version.setToolTipText("Modpack Versions");
		add(version);
	}

	@Override public void onVisible() { }

	/*
	 * GUI Code to add a modpack to the selection
	 */
	public static void addPack(ModPack pack) {
		if (!modPacksAdded) {
			modPacksAdded = true;
			packs.removeAll();
		}
		final int packIndex = packPanels.size();
		final JPanel p = new JPanel();
		p.setBounds(0, (packIndex * 55), 420, 55);
		p.setLayout(null);
		JLabel logo = new JLabel(new ImageIcon(pack.getLogo()));
		logo.setBounds(6, 6, 42, 42);
		logo.setVisible(true);

		JTextArea filler = new JTextArea(pack.getName() + " (v" + pack.getVersion() + ") Minecraft Version " + pack.getMcVersion() + "\n" + "Compiled By " + pack.getAuthor());
		filler.setBorder(null);
		filler.setEditable(false);
		filler.setForeground(Color.white);
		filler.setBounds(58, 6, 378, 42);
		filler.setBackground(new Color(255, 255, 255, 0));
		MouseListener lin = new MouseListener() {
			@Override public void mouseClicked(MouseEvent e) {
				selectedPack = packIndex;
				updatePacks();
			}
			@Override public void mouseReleased(MouseEvent e) { }
			@Override public void mousePressed(MouseEvent e) { 
				selectedPack = packIndex;
				updatePacks();
			}
			@Override public void mouseExited(MouseEvent e) { }
			@Override public void mouseEntered(MouseEvent e) { }
		};
		p.addMouseListener(lin);
		filler.addMouseListener(lin);
		logo.addMouseListener(lin);
		p.add(filler);
		p.add(logo);
		packPanels.add(p);
		packs.add(p);
		if(currentPacks.isEmpty()) {
			packs.setMinimumSize(new Dimension(420, (ModPack.getPackArray().size() * 55)));
			packs.setPreferredSize(new Dimension(420, (ModPack.getPackArray().size() * 55)));
		} else {
			packs.setMinimumSize(new Dimension(420, (currentPacks.size() * 55)));
			packs.setPreferredSize(new Dimension(420, (currentPacks.size() * 55)));
		}
		packsScroll.revalidate();
		if(pack.getDir().equalsIgnoreCase(Settings.getSettings().getLastPack())) {
			selectedPack = packIndex;
		}
	}

	@Override
	public void onModPackAdded(ModPack pack) {
		addPack(pack);
		Logger.logInfo("Adding pack " + packPanels.size());
		updatePacks();
	}

	public static void sortPacks() {
		packPanels.clear();
		packs.removeAll();
		currentPacks.clear();
		int counter = 0;
		selectedPack = 0;
		packInfo.setText("");
		packs.repaint();
		for(ModPack pack : ModPack.getPackArray()) {
			if(originCheck(pack) && mcVersionCheck(pack) && avaliabilityCheck(pack) && textSearch(pack)) {
				currentPacks.put(counter, pack);
				addPack(pack);
				counter++;
			}
		}
		updatePacks();
	}

	private static void updatePacks() {
		for(int i = 0; i < packPanels.size(); i++) {
			if(selectedPack == i) {
				ModPack pack = ModPack.getPack(getIndex());
				if(pack != null) {
					String mods = "";
					if(pack.getMods() != null) {
						mods += "<p>This pack contains the following mods by default:</p><ul>";
						for (String name : ModPack.getPack(getIndex()).getMods()) {
							mods += "<li>" + name + "</li>";
						}
						mods += "</ul>";
					}
					packPanels.get(i).setBackground(UIManager.getColor("control").darker().darker());
					packPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					File tempDir = new File(OSUtils.getDynamicStorageLocation(), "ModPacks" + File.separator + ModPack.getPack(getIndex()).getDir());
					packInfo.setText("<html><img src=' file:\\"+ tempDir.getPath() + File.separator + ModPack.getPack(getIndex()).getImageName() +"' width=400 height=200></img> <br>" + ModPack.getPack(getIndex()).getInfo() + mods);
					packInfo.setCaretPosition(0);
					
					String tempVer = Settings.getSettings().getPackVer();
					version.removeAllItems();
					version.addItem("Recommended");
					if(pack.getOldVersions() != null) {
						for(String s : pack.getOldVersions()) {
							version.addItem(s);
						}
						version.setSelectedItem(tempVer);
					}
				}
			} else {
				packPanels.get(i).setBackground(UIManager.getColor("control"));
				packPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
		}
	}

	public static int getSelectedModIndex() {
		return modPacksAdded ? getIndex() : -1;
	}


	private static int getIndex() {
		return (!currentPacks.isEmpty()) ? currentPacks.get(selectedPack).getIndex() : selectedPack;
	}

	public void updateLocale() {
		editModPack.setText(I18N.getLocaleString("MODS_EDIT_PACK"));
	}

	private static boolean avaliabilityCheck(ModPack pack) {
		return (avaliability.equalsIgnoreCase("all")) || (avaliability.equalsIgnoreCase("public") && !pack.isPrivatePack()) || (avaliability.equalsIgnoreCase("private") && pack.isPrivatePack());
	}

	private static boolean mcVersionCheck(ModPack pack) {
		return (mcVersion.equalsIgnoreCase("all")) || (mcVersion.equalsIgnoreCase(pack.getMcVersion()));
	}

	private static boolean originCheck(ModPack pack) {
		return (origin.equalsIgnoreCase("all")) || (origin.equalsIgnoreCase("ftb") && pack.getAuthor().equalsIgnoreCase("the ftb team")) || (origin.equalsIgnoreCase("3rd party") && !pack.getAuthor().equalsIgnoreCase("the ftb team"));
	}

	private static boolean textSearch(ModPack pack) {
		String searchString = SearchDialog.lastPackSearch.toLowerCase();
		return ((searchString.isEmpty()) || pack.getName().toLowerCase().contains(searchString) || pack.getAuthor().toLowerCase().contains(searchString));
	}
}