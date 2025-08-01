package edu.jiangxin.apktoolbox.convert.color.colortable;

public class RalColorTable implements IColorTable {

	@Override
	public String toString() {
		return "Ral Colors";
	}

	@Override
	public String[] getColumnNames() {
		return COLUMN_NAMES;
	}

	@Override
	public String[][] getTableRowData() {
		return TABLE_ROW_DATA;
	}

	@Override
	public int getLabelIndex() {
		return 0;
	}

	@Override
	public int getHexIndex() {
		return 3;
	}

	public static final String[] COLUMN_NAMES = { "颜色", "英文", "中文", "RGB代码" };

	public static final String[][] TABLE_ROW_DATA = { { "RAL 1000", "Green beige", "米绿色", "#BEBD7F" },
			{ "RAL 1001", "Beige", "米色", "#C2B078" }, { "RAL 1002", "Sand yellow", "沙黄色", "#C6A664" },
			{ "RAL 1003", "Signal yellow", "信号黄", "#E5BE01" }, { "RAL 1004", "Golden yellow", "金黄色", "#CDA434" },
			{ "RAL 1005", "Honey yellow", "蜜黄色", "#A98307" }, { "RAL 1006", "Maize yellow", "玉米黄", "#E4A010" },
			{ "RAL 1007", "Daffodil yellow", "水仙黄", "#DC9D00" }, { "RAL 1011", "Brown beige", "米褐色", "#8A6642" },
			{ "RAL 1012", "Lemon yellow", "柠檬黄", "#C7B446" }, { "RAL 1013", "Oyster white", "牡蛎白", "#EAE6CA" },
			{ "RAL 1014", "Ivory", "象牙色", "#E1CC4F" }, { "RAL 1015", "Light ivory", "浅象牙色", "#E6D690" },
			{ "RAL 1016", "Sulfur yellow", "硫磺色", "#EDFF21" }, { "RAL 1017", "Saffron yellow", "深黄色", "#F5D033" },
			{ "RAL 1018", "Zinc yellow", "绿黄色", "#F8F32B" }, { "RAL 1019", "Grey beige", "米灰色", "#9E9764" },
			{ "RAL 1020", "Olive yellow", "橄榄黄", "#999950" }, { "RAL 1021", "Rape yellow", "油菜黄", "#F3DA0B" },
			{ "RAL 1023", "Traffic yellow", "交通黄", "#FAD201" }, { "RAL 1024", "Ochre yellow", "赭黄色", "#AEA04B" },
			{ "RAL 1026", "Luminous yellow", "亮黄色", "#FFFF00" }, { "RAL 1027", "Curry", "咖喱色", "#9D9101" },
			{ "RAL 1028", "Melon yellow", "浅橙黄", "#F4A900" }, { "RAL 1032", "Broom yellow", "金雀花黄", "#D6AE01" },
			{ "RAL 1033", "Dahlia yellow", "大丽花黄", "#F3A505" }, { "RAL 1034", "Pastel yellow", "淡黄色", "#EFA94A" },
			{ "RAL 1035", "Pearl beige", "浅珍珠", "#6A5D4D" }, { "RAL 1036", "Pearl gold", "珍珠金", "#705335" },
			{ "RAL 1037", "Sun yellow", "日光黄", "#F39F18" }, { "RAL 2000", "Yellow orange", "黄橙色", "#ED760E" },
			{ "RAL 2001", "Red orange", "橙红", "#C93C20" }, { "RAL 2002", "Vermilion", "朱红", "#CB2821" },
			{ "RAL 2003", "Pastel orange", "淡橙", "#FF7514" }, { "RAL 2004", "Pure orange", "纯橙", "#F44611" },
			{ "RAL 2005", "Luminous orange", "亮橙", "#FF2301" },
			{ "RAL 2007", "Luminous bright orange", "闪亮橙", "#FFA420" },
			{ "RAL 2008", "Bright red orange", "亮红橙", "#F75E25" }, { "RAL 2009", "Traffic orange", "交通橙", "#F54021" },
			{ "RAL 2010", "Signal orange", "信号橙", "#D84B20" }, { "RAL 2011", "Deep orange", "深橙色", "#EC7C26" },
			{ "RAL 2012", "Salmon range", "鲑鱼橙", "#E55137" }, { "RAL 2013", "Pearl orange", "珍珠橙", "#C35831" },
			{ "RAL 3000", "Flame red", "火焰红", "#AF2B1E" }, { "RAL 3001", "Signal red", "信号红", "#A52019" },
			{ "RAL 3002", "Carmine red", "胭脂红", "#A2231D" }, { "RAL 3003", "Ruby red", "宝石红", "#9B111E" },
			{ "RAL 3004", "Purple red", "紫红色", "#75151E" }, { "RAL 3005", "Wine red", "葡萄酒红", "#5E2129" },
			{ "RAL 3007", "Black red", "黑红色", "#412227" }, { "RAL 3009", "Oxide red", "氧化红", "#642424" },
			{ "RAL 3011", "Brown red", "棕红色", "#781F19" }, { "RAL 3012", "Beige red", "米红色", "#C1876B" },
			{ "RAL 3013", "Tomato red", "番茄红", "#A12312" }, { "RAL 3014", "Antique pink", "古粉红色", "#D36E70" },
			{ "RAL 3015", "Light pink", "淡粉红色", "#EA899A" }, { "RAL 3016", "Coral red", "珊瑚红色", "#B32821" },
			{ "RAL 3017", "Rose", "玫瑰色", "#E63244" }, { "RAL 3018", "Strawberry red", "草莓红", "#D53032" },
			{ "RAL 3020", "Traffic red", "交通红", "#CC0605" }, { "RAL 3022", "Salmon pink", "三文鱼肉色", "#D95030" },
			{ "RAL 3024", "Luminous red", "亮红色", "#F80000" }, { "RAL 3026", "Luminous", "淡亮红色", "#FE0000" },
			{ "RAL 3027", "Raspberry red", "紫红色", "#C51D34" }, { "RAL 3028", "Pure  red", "东方红", "#B32428" },
			{ "RAL 3032", "Pearl ruby red", "珠宝红", "#721422" }, { "RAL 3033", "Pearl pink", "珍珠粉", "#B44C43" },
			{ "RAL 4001", "Red lilac", "丁香红", "#6D3F5B" }, { "RAL 4002", "Red violet", "紫红色", "#922B3E" },
			{ "RAL 4003", "Heather violet", "石南紫", "#DE4C8A" }, { "RAL 4004", "Claret violet", "酒红紫", "#641C34" },
			{ "RAL 4005", "Blue lilac", "丁香蓝", "#6C4675" }, { "RAL 4006", "Traffic purple", "交通紫", "#A03472" },
			{ "RAL 4007", "Purple violet", "紫红蓝色", "#4A192C" }, { "RAL 4008", "Signal violet", "信号紫罗兰", "#924E7D" },
			{ "RAL 4009", "Pastel violet", "崧蓝紫色", "#A18594" }, { "RAL 4010", "Telemagenta", "淡品红色", "#CF3476" },
			{ "RAL 4011", "Pearl violet", "珍珠紫", "#8673A1" }, { "RAL 4012", "Pearl black berry", "珍珠黑莓", "#6C6874" },
			{ "RAL 5000", "Violet blue", "紫蓝色", "#354D73" }, { "RAL 5001", "Green blue", "蓝绿色", "#1F3438" },
			{ "RAL 5002", "Ultramarine blue", "海青蓝", "#20214F" }, { "RAL 5003", "Saphire blue", "蓝宝石蓝", "#1D1E33" },
			{ "RAL 5004", "Black blue", "蓝黑色", "#18171C" }, { "RAL 5005", "Signal blue", "信号蓝", "#1E2460" },
			{ "RAL 5007", "Brillant blue", "亮蓝色", "#3E5F8A" }, { "RAL 5008", "Grey blue", "灰蓝色", "#26252D" },
			{ "RAL 5009", "Azure blue", "天青蓝", "#025669" }, { "RAL 5010", "Gentian blue", "龙胆蓝色", "#0E294B" },
			{ "RAL 5011", "Steel blue", "钢蓝色", "#231A24" }, { "RAL 5012", "Light blue", "淡蓝色", "#3B83BD" },
			{ "RAL 5013", "Cobalt blue", "钴蓝色", "#1E213D" }, { "RAL 5014", "Pigeon blue", "鸽蓝色", "#606E8C" },
			{ "RAL 5015", "Sky blue", "天蓝色", "#2271B3" }, { "RAL 5017", "Traffic blue", "交通蓝", "#063971" },
			{ "RAL 5018", "Turquoise blue", "绿松石蓝", "#3F888F" }, { "RAL 5019", "Capri blue", "变幻蓝", "#1B5583" },
			{ "RAL 5020", "Ocean blue", "海蓝色", "#1D334A" }, { "RAL 5021", "Water blue", "水蓝", "#256D7B" },
			{ "RAL 5022", "Night blue", "夜蓝色", "#252850" }, { "RAL 5023", "Distant blue", "远蓝色", "#49678D" },
			{ "RAL 5024", "Pastel blue", "浅蓝色", "#5D9B9B" }, { "RAL 5025", "Pearl gentian blue", "珍珠龙胆蓝", "#2A6478" },
			{ "RAL 5026", "Pearl night blue", "珍珠夜蓝", "#102C54" }, { "RAL 6000", "Patina green", "铜锈绿色", "#316650" },
			{ "RAL 6001", "Emerald green", "翡翠绿色", "#287233" }, { "RAL 6002", "Leaf green", "叶绿色", "#2D572C" },
			{ "RAL 6003", "Olive green", "橄榄绿", "#424632" }, { "RAL 6004", "Blue green", "蓝绿色", "#1F3A3D" },
			{ "RAL 6005", "Moss green", "苔藓绿", "#2F4538" }, { "RAL 6006", "Grey olive", "橄榄灰绿", "#3E3B32" },
			{ "RAL 6007", "Bottle green", "瓶绿", "#343B29" }, { "RAL 6008", "Brown green", "褐绿", "#39352A" },
			{ "RAL 6009", "Fir green", "冷杉绿", "#31372B" }, { "RAL 6010", "Grass green", "草绿色", "#35682D" },
			{ "RAL 6011", "Reseda green", "淡橄榄绿", "#587246" }, { "RAL 6012", "Black green", "墨绿色", "#343E40" },
			{ "RAL 6013", "Reed green", "芦苇绿", "#6C7156" }, { "RAL 6014", "Yellow olive", "黄橄榄色", "#47402E" },
			{ "RAL 6015", "Black olive", "黑橄榄色", "#3B3C36" }, { "RAL 6016", "Turquoise green", "青绿色", "#1E5945" },
			{ "RAL 6017", "May green", "五月红", "#4C9141" }, { "RAL 6018", "Yellow green", "黄绿色", "#57A639" },
			{ "RAL 6019", "Pastel green", "水粉绿", "#BDECB6" }, { "RAL 6020", "Chrome green", "铭绿色", "#2E3A23" },
			{ "RAL 6021", "Pale green", "淡绿色", "#89AC76" }, { "RAL 6022", "Olive drab", "橄榄土褐色", "#25221B" },
			{ "RAL 6024", "Traffic green", "交通绿", "#308446" }, { "RAL 6025", "Fern green", "蕨绿色", "#3D642D" },
			{ "RAL 6026", "Opal green", "猫眼绿", "#015D52" }, { "RAL 6027", "Light green", "浅绿色", "#84C3BE" },
			{ "RAL 6028", "Pine green", "松绿色", "#2C5545" }, { "RAL 6029", "Mint green", "薄荷绿", "#20603D" },
			{ "RAL 6032", "Signal green", "信号绿", "#317F43" }, { "RAL 6033", "Mint turquoise", "薄荷青", "#497E76" },
			{ "RAL 6034", "Pastel turquoise", "崧蓝青", "#7FB5B5" }, { "RAL 6035", "Pearl green", "珍珠绿", "#1C542D" },
			{ "RAL 6036", "Pearl opal green", "珍珠猫眼绿", "#193737" }, { "RAL 6037", "Pure green", "纯绿", "#008F39" },
			{ "RAL 6038", "Luminous green", "亮绿", "#00BB2D" }, { "RAL 7000", "Squirrel grey", "松鼠灰", "#78858B" },
			{ "RAL 7001", "Silver grey", "银灰色", "#8A9597" }, { "RAL 7002", "Olive grey", "橄榄灰绿色", "#7E7B52" },
			{ "RAL 7003", "Moss grey", "苔藓绿", "#6C7059" }, { "RAL 7004", "Signal grey", "信号灰", "#969992" },
			{ "RAL 7005", "Mouse grey", "鼠灰色", "#646B63" }, { "RAL 7006", "Beige grey", "米灰色", "#6D6552" },
			{ "RAL 7008", "Khaki grey", "土黄灰色", "#6A5F31" }, { "RAL 7009", "Green grey", "绿灰色", "#4D5645" },
			{ "RAL 7010", "Tarpaulin grey", "油布灰", "#4C514A" }, { "RAL 7011", "Iron grey", "铁灰色", "#434B4D" },
			{ "RAL 7012", "Basalt grey", "玄武石灰", "#4E5754" }, { "RAL 7013", "Brown grey", "褐灰色", "#464531" },
			{ "RAL 7015", "Slate grey", "浅橄榄灰", "#434750" }, { "RAL 7016", "Anthracite grey", "煤灰", "#293133" },
			{ "RAL 7021", "Black grey", "黑灰", "#23282B" }, { "RAL 7022", "Umbra grey", "暗灰", "#332F2C" },
			{ "RAL 7023", "Concrete grey", "混凝土灰", "#686C5E" }, { "RAL 7024", "Graphite grey", "石墨灰", "#474A51" },
			{ "RAL 7026", "Granite grey", "花岗灰", "#2F353B" }, { "RAL 7030", "Stone grey", "石灰色", "#8B8C7A" },
			{ "RAL 7031", "Blue grey", "蓝灰色", "#474B4E" }, { "RAL 7032", "Pebble grey", "卵石灰", "#B8B799" },
			{ "RAL 7033", "Cement grey", "水泥灰", "#7D8471" }, { "RAL 7034", "Yellow grey", "黄灰色", "#8F8B66" },
			{ "RAL 7035", "Light grey", "浅灰色", "#D7D7D7" }, { "RAL 7036", "Platinum grey", "铂灰色", "#7F7679" },
			{ "RAL 7037", "Dusty grey", "土灰色", "#7D7F7D" }, { "RAL 7038", "Agate grey", "玛瑙灰", "#B5B8B1" },
			{ "RAL 7039", "Quartz grey", "石英灰", "#6C6960" }, { "RAL 7040", "Window grey", "窗灰色", "#9DA1AA" },
			{ "RAL 7042", "Traffic grey A", "交通灰A", "#8D948D" }, { "RAL 7043", "Traffic grey B", "交通灰B", "#4E5452" },
			{ "RAL 7044", "Silk grey", "丝绸灰色", "#CAC4B0" }, { "RAL 7045", "Telegrey 1", "浅灰1", "#909090" },
			{ "RAL 7046", "Telegrey 2", "浅灰2", "#82898F" }, { "RAL 7047", "Telegrey 4", "浅灰4", "#D0D0D0" },
			{ "RAL 7048", "Pearl mouse grey", "珍珠鼠灰", "#898176" }, { "RAL 8000", "Green brown", "绿褐色", "#826C34" },
			{ "RAL 8001", "Ochre brown", "赭石棕色", "#955F20" }, { "RAL 8002", "Signal brown", "信号褐", "#6C3B2A" },
			{ "RAL 8003", "Clay brown", "土棕褐色", "#734222" }, { "RAL 8004", "Copper brown", "铜褐色", "#8E402A" },
			{ "RAL 8007", "Fawn brown", "鹿褐色", "#59351F" }, { "RAL 8008", "Olive brown", "橄榄棕色", "#6F4F28" },
			{ "RAL 8011", "Nut brown", "深棕色", "#5B3A29" }, { "RAL 8012", "Red brown", "红褐色", "#592321" },
			{ "RAL 8014", "Sepia brown", "乌贼棕色", "#382C1E" }, { "RAL 8015", "Chestnut brown", "栗色", "#633A34" },
			{ "RAL 8016", "Mahogany brown", "红木棕色", "#4C2F27" }, { "RAL 8017", "Chocolate brown", "巧克力棕色", "#45322E" },
			{ "RAL 8019", "Grey brown", "灰褐色", "#403A3A" }, { "RAL 8022", "Black brown", "黑褐色", "#212121" },
			{ "RAL 8023", "Orange brown", "桔黄褐", "#A65E2E" }, { "RAL 8024", "Beige brown", "哔叽棕色", "#79553D" },
			{ "RAL 8025", "Pale brown", "浅褐色", "#755C48" }, { "RAL 8028", "Terra brown", "浅灰褐色", "#4E3B31" },
			{ "RAL 8029", "Pearl copper", "珍珠铜", "#763C28" }, { "RAL 9001", "Cream", "奶油色", "#FDF4E3" },
			{ "RAL 9002", "Grey white", "灰白色", "#E7EBDA" }, { "RAL 9003", "Signal white", "信号白", "#F4F4F4" },
			{ "RAL 9004", "Signal black", "信号黑", "#282828" }, { "RAL 9005", "Jet black", "乌黑色", "#0A0A0A" },
			{ "RAL 9006", "White aluminium", "白铝灰色", "#A5A5A5" }, { "RAL 9007", "Grey aluminium", "灰铝色", "#8F8F8F" },
			{ "RAL 9010", "Pure white", "纯白色", "#FFFFFF" }, { "RAL 9011", "Graphite black", "石墨黑", "#1C1C1C" },
			{ "RAL 9016", "Traffic white", "交通白", "#F6F6F6" }, { "RAL 9017", "Traffic black", "交通黑", "#1E1E1E" },
			{ "RAL 9018", "Papyrus white", "草纸白", "#D7D7D7" }, { "RAL 9022", "Pearl light grey", "珍珠浅灰", "#9C9C9C" },
			{ "RAL 9023", "Pearl dark grey", "珍珠深灰", "#828282" } };
}
