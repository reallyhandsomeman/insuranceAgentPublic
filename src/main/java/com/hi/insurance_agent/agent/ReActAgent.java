package com.hi.insurance_agent.agent;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct (Reasoning and Acting) 模式的代理抽象类
 * 实现了思考-行动的循环模式
 */
@Slf4j
@Data
// @EqualsAndHashCode(callSuper = true)是 Lombok 提供的注解，用于自动生成 equals()和 hashCode()方法，并且包含父类的字段进行比较和哈希计算。
@EqualsAndHashCode(callSuper = true)
public abstract class ReActAgent extends BaseAgent{

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     *
     * @return 行动执行结果
     */
    public abstract String act();

    /**
     * 执行单个步骤：思考和行动
     *
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                String response = getMessageList().getLast().getText();
                setAgentState(AgentState.FINISHED);
                return "思考完成 - 无需行动\n" + response;
            }
            return act();
        } catch (Exception e) {
            // 记录异常日志
            log.error("单步执行失败：{}", e.getMessage());
            return "步骤执行失败: " + e.getMessage();
        }
    }
}
