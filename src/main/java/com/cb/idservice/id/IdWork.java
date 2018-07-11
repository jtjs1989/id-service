package com.cb.idservice.id;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * * 0          41     51          64
 *  +-----------+------+------------+
 *  |timestamp  | node |increment   |
 *  +-----------+------+------------+
 * @author chenbo (jtjs1989@163.com)
 * 
 * 前面0-41位为时间戳偏移值， 选择一个基准时间戳  基准时间戳约接近当前时间生成的ID值约小，长度也越短
 * 42-51 位为机器码 生成的方式可以有多种，这儿使用本机IP地址的hashcode对1024得到，一个集群中要保证每台机器的node值不一样，否则可能出新重复ID
 * 最后12位递增数字  增长到4095就归零， 如果同一台服务器上同一毫秒生成的ID数量超过 4096则会重复  （个人认为这样的情况实际中不会出现）
 *
 */
public class IdWork {

	private final int nodeId;
	//nodeId 向左移位的位数
	private final int nodeIdShift = 12;
	
	private AtomicInteger increment = new AtomicInteger();
	
	private final int maxIncrement = (1 << 12) -1;
	// 时间戳向左移位的位数
	private final int timestampShift = 22;
	
	//选取的时间 零点 这儿选择 2018-1-1 00:00:00 为起点
	private final long zoroTimestamp = 1514736000340L;
	
	public IdWork(int nodeId) {
		this.nodeId = nodeId;
	}
	
	public long nextId() {
		long timestamp = System.currentTimeMillis();
		return ((timestamp-zoroTimestamp) << timestampShift) | (nodeId << nodeIdShift) | getAndIncreament();
	}
	/**
	 * 通过循环生成0-maxIncrement的数字来避免加锁
	 * 如果一毫米内生成的ID超过maxIncrement 则会产生重复数字
	 * 考虑实际应用场景同一毫秒能不可能有这么大的并发量，因此此处不考虑
	 * @return
	 */
	private final int getAndIncreament() {
		for (;;) {
			int current = increment.get();
			int next = (current >= maxIncrement ? 0 : current + 1);
			if (increment.compareAndSet(current, next)) {
				return next;
			}
		}
	}
}
