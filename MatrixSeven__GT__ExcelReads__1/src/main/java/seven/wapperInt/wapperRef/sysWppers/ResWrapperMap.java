package seven.wapperInt.wapperRef.sysWppers;

import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seven.util.ExcelTool;
import seven.util.RegHelper;
import seven.config.Config;
import seven.wapperInt.wapperRef.WrapperObj;

import java.util.*;
import java.util.function.Consumer;

//=======================================================
//		          .----.
//		       _.'__    `.
//		   .--(^)(^^)---/#\
//		 .' @          /###\
//		 :         ,   #####
//		 :         ,   #####
//		  `-..__.-' _.-\###/
//		        `;_:    `"'
//		      .'"""""`.
//		     /,  ya ,\\
//		    //狗神保佑  \\
//		    `-._______.-'
//		    ___`. | .'___
//		   (______|______)
//=======================================================

/**
 * @author Seven<p>
 */
@SuppressWarnings({ "all" })
public class ResWrapperMap extends WrapperObj<Map<String, String>> {

	private static final Logger logger = LoggerFactory.getLogger(ResWrapperMap.class);

	public ResWrapperMap(Consumer<Config> consumer) {
		super(consumer);
	}

	@Override
	protected <T> T RefResWrapper(String fs, boolean isMap, String key) throws Exception {
		config.check();
		HashMap<String, Map> maps = null;
		List<Map> list = null;
		if (isMap) {
			maps = new HashMap<>(config.getVocSize());
		} else {
			list = new ArrayList<>(config.getVocSize());
		}

		Map<String, String> map;
		String[] titles;
		Sheet sheet;
		Row row;
		String[] require = config.getRequire();
		Workbook hhf = ExcelTool.newInstance(fs, false);
		int start_sheet = config.getStartSheet();
		int end_sheet = start_sheet + 1;
		if (config.getIsLoopSheet()) {
			end_sheet = config.getEndSheet() == null ? hhf.getNumberOfSheets() : config.getEndSheet();
			if (end_sheet <= 0 || end_sheet > hhf.getNumberOfSheets()) {
				logger.error("sheet范围不正确");
				throw new Exception("sheet范围不正确");
			}
		}
		for (; start_sheet < end_sheet; start_sheet++) {

			sheet = hhf.getSheetAt(start_sheet);
			row = sheet.getRow(config.getTitleRow());
			titles = new String[row.getPhysicalNumberOfCells()];
			for (int i = 0, rows = row.getPhysicalNumberOfCells(); i < rows; i++) {
				titles[i] = getCellFormatValue(row.getCell((short) i));
			}
			try {
				if (row.getPhysicalNumberOfCells() == 0) {
					logger.error("列表头获取失败");
					throw new Exception("列表头获取失败");
				}
			} catch (Exception e) {
				logger.error("列表头获取失败,sheet:{} sheetNmae:{}", start_sheet, sheet.getSheetName());
				continue;
			}

			int start = config.getContentRowStart();
			int rowNum = Objects.isNull(config.getContentRowEnd()) ? sheet.getLastRowNum() : config.getContentRowEnd();
			for (; start < rowNum; start++) {
				row = sheet.getRow(start);
				String Map_key = "";
				if (null != row) {
					map = new HashMap<>();
					try {
						if (titles.length < row.getPhysicalNumberOfCells()) {
							logger.error("列表长度小于实际列长度");
							throw new Exception("列表长度小于实际列长度");
						}
						if (require != null && (titles.length != require.length)) {
							logger.error("验证规则长度不对");
							throw new Exception("验证规则长度不对");
						}
					} catch (Exception e) {
						logger.error("列表长度小于实际列长度 startSheet:{},sheetName:{}, row:{}", start_sheet,
								sheet.getSheetName(), rowNum);
					}
					for (int j = 0, colNum = row.getPhysicalNumberOfCells(); j < colNum; j++) {
						if (require != null && !(require[j].equals("Null")) && filterColByKey.contains(titles[j])) {
							if (RegHelper.require(require[j], getCellFormatValue(row.getCell((short) j)))) {
								map.put(titles[j], getCellFormatValue(row.getCell((short) j)));
							} else {
							}
						} else {
							map.put(titles[j], getCellFormatValue(row.getCell((short) j)));
						}
					}
					if (!isNull(map))
						if (this.filter.test(map)) {
							continue;
						}
					this.process.accept(map);
					if (isMap) {
						maps.put(map.get(key), map);
					} else {
						list.add(map);
					}

				}
			}
		}
		if (!isMap) {
			if (c != null) {
				list.sort(c);
			}
			return (T) list;
		}
		return (T) maps;
	}
}
