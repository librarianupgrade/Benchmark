package cn.cerc.ui.core;

import cn.cerc.core.DataSet;
import cn.cerc.ui.grid.MutiPage;
import cn.cerc.ui.other.BeanRecord;
import cn.cerc.ui.other.BuildRecord;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class MutiGrid<T> {
	private DataSet dataSet;
	// 支持表格分页
	private MutiPage pages = new MutiPage();
	private List<T> items = new ArrayList<T>();

	public MutiGrid(DataSet dataSet) {
		this.setDataset(dataSet);
	}

	public DataSet getDataset() {
		return dataSet;
	}

	public void setDataset(DataSet dataset) {
		this.dataSet = dataset;
		pages.setDataSet(dataSet);
	}

	public int map(HttpServletRequest req, Class<T> clazz) {
		return this.map(req, clazz, null, true);
	}

	public int map(HttpServletRequest request, Class<T> clazz, BuildRecord make, boolean defProcess) {
		BeanRecord<T> defMake = null;
		try {
			if (defProcess) {
				defMake = new BeanRecord<T>();
			}
			T item = null;
			if (dataSet.size() == 0) {
				return 0;
			}
			pages.setRequest(request);

			int i = pages.getBegin();
			while (i <= pages.getEnd()) {
				dataSet.setRecNo(i + 1);
				item = clazz.getDeclaredConstructor().newInstance();
				if (defProcess) {
					defMake.build(item, dataSet.getCurrent());
				}
				if (make != null) {
					make.build(item, dataSet.getCurrent());
				}

				items.add(item);
				i++;
			}
			return items.size();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			if (defMake != null) {
				defMake.close();
			}
		}
	}

	public List<T> getList() {
		return items;
	}

	public MutiPage getPages() {
		return pages;
	}

}
