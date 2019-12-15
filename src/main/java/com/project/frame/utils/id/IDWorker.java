package com.project.frame.utils.id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * IDWorker
 *
 * @author mxy
 * @date 2019/12/15
 */
@Component
public class IDWorker implements Serializable {

    private static final long serialVersionUID = -322371382575481443L;

    private static final Logger LOG = LoggerFactory.getLogger(IDWorker.class);

    //工作中心ID
    private long workerId;
    //数据中心ID
    private long datacenterId;
    //自增长序列
    private long sequence = 0L;

    //时间戳基数--2016-04-13 10:43
    private long twepoch = 1460515462699L;

    private long workerIdBits = 5L;
    private long datacenterIdBits = 5L;
    private long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    private long sequenceBits = 12L;

    private long workerIdShift = sequenceBits;
    private long datacenterIdShift = sequenceBits + workerIdBits;
    private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private long sequenceMask = -1L ^ (-1L << sequenceBits);

    private long lastTimestamp = -1L;

    public IDWorker() {
    }

    public IDWorker(long workerId, long datacenterId) {

        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("workerId必须在0-%d之间", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenterId必须在0-%d之间", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        LOG.info(String.format("IDWorker初始化完成. 时间戳偏移量 %d, datacenterId位数 %d, workerId位数 %d, 自增序列位数 %d, workerid %d", timestampLeftShift, datacenterIdBits, workerIdBits, sequenceBits, workerId));
    }

    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            LOG.error(String.format("时钟被修改，拒绝请求，直到 %d.", lastTimestamp));
            throw new RuntimeException(String.format("时钟被修改，拒绝为毫秒数%d生成ID", lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift) | (workerId << workerIdShift) | sequence;
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }
}