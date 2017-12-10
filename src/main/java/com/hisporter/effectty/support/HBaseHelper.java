package com.hisporter.effectty.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HBaseHelper {
	public static Configuration config = HBaseConfiguration.create();
	private static String family = "c1";
	private static String column = "d1";
	private static HConnection hconnection;
	private static Logger log = LoggerFactory.getLogger(HBaseHelper.class);

	static {
		try {
			ExecutorService pool = Executors.newCachedThreadPool();
			hconnection = HConnectionManager.createConnection(config, pool);
		} catch (IOException e) {
			log.debug(e.getMessage());
		}
	}

	public static HConnection getHconnection() {
		return hconnection;
	}

	public static List<String> findByIndex(String indexTable, String startRow, String endRow, int caching) throws IOException {
		List<String> outList = new ArrayList<String>();
		ResultScanner rs = null;
		HTableInterface table = null;
		try {
			long t0 = SystemClock.now();
			log.debug("indexTable=" + indexTable);
			table = hconnection.getTable(indexTable);
			long t1 = SystemClock.now();
			log.debug("打开表getTable消耗时间：" + (t1 - t0) + " ms");
			Scan scan = new Scan();
			scan.setStartRow(Bytes.toBytes(startRow));
			scan.setStopRow(Bytes.toBytes(endRow));
			scan.setCaching(caching);
			scan.addFamily(Bytes.toBytes(family));
			//scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));

			t1 = SystemClock.now();
			rs = table.getScanner(scan);
			long t2 = SystemClock.now();
			log.debug("检索Hbase集群中数据 getScanner 消耗时间：" + (t2 - t1) + " ms");

			long hbaseValueLen = 0;
			for (Result result : rs) {
				Cell[] cells = result.rawCells();
				String value = Bytes.toString(CellUtil.cloneValue(cells[0]));
				//log.debug("=====value:[" + value + "]");
				outList.add(value);
				hbaseValueLen += value.length();
			}
			long t3 = SystemClock.now();
			log.debug("获取结果中的数据 getValue 消耗时间：" + (t3 - t2) + " ms, hbasevaluelen= " + hbaseValueLen );

		} finally {
			if (rs != null) {
				rs.close();
			}
			if (table != null) {
				table.close();
			}
		}

		return outList;

	}

}
