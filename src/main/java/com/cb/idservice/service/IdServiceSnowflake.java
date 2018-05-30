package com.cb.idservice.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.cb.idservice.api.IdService;
import com.cb.idservice.id.IdWork;

public class IdServiceSnowflake implements IdService {

	private IdWork idWord;
	
	public IdServiceSnowflake() {
		this.idWord = new IdWork(getNodeId());
	}
	public long genId() {
		return idWord.nextId();
	}

	private int getNodeId() {
		try {
			InetAddress add = InetAddress.getLocalHost();
			return Math.abs(add.getHostAddress().hashCode()) & 1023;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new RuntimeException("获取本地IP地址失败");
		}
	}
}
