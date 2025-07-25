package seven.savewapper.wapperRef.sysWppers;
//=======================================================

//		          .----.
//		       _.'__    `.
//		   .--(^)(^^)---/!\
//		 .' @          /!!!\
//		 :         ,    !!!!
//		  `-..__.-' _.-\!!!/
//		        `;_:    `"'
//		      .'"""""`.
//		     /,  ya ,\\
//		    //狗神保佑\\
//		    `-._______.-'
//		    ___`. | .'___
//		   (______|______)
//=======================================================

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * [Zhihu]https://www.zhihu.com/people/Sweets07
 * [Github]https://github.com/MatrixSeven
 * Created by seven on 2017/1/1.
 */
public class ResExportDBMap extends ResExportMap {

	public ResExportDBMap(ResultSet resultSet, String path) {
		super(resultSet, path);
	}

	public ResExportDBMap(ResultSet resultSet) {
		super(resultSet);
	}

	public ResExportMap CreateList() throws Exception {
		this.list = new ArrayList<>();
		HashMap<String, String> stringStringHashMap;
		if (resultSet != null) {
			ResultSetMetaData res = resultSet.getMetaData();
			int index = res.getColumnCount() + 1;
			while (resultSet.next()) {
				stringStringHashMap = new HashMap<>();
				for (int i = 1; i < index; i++) {
					stringStringHashMap.put(res.getColumnName(i), resultSet.getString(res.getColumnName(i)));
				}
				list.add(stringStringHashMap);
			}
		}
		return this;
	}

	@Override
	public void Save() throws Exception {
		this.CreateList();
		super.Save();
	}
}
