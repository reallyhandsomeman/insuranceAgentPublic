package com.hi.insurance_agent.agent;

// enum（枚举）是一种特殊的类，用于定义一组固定的常量。AgentState枚举定义了代理执行状态的几种可能值。
public enum AgentState {
    /**
     * 空闲状态
     */
    IDLE,

    /**
     * 运行中状态
     */
    RUNNING,

    /**
     * 已完成状态
     */
    FINISHED,

    /**
     * 错误状态
     */
    ERROR
}
