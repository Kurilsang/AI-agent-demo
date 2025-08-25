-- ================================================================
-- AI Agent Station 完整数据库初始化脚本
-- 基于现有代码结构和测试数据重新构建
-- 版本: 2025.08.25.v2 (修复AutoAgent流程配置缺失问题)
-- 目标: 创建一个可直接运行的完整数据库，支持所有AI Agent功能
-- 修复: 添加智能体3流程配置清理逻辑，确保Docker MySQL环境正常运行
-- ================================================================

-- 设置字符集和SQL模式
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE='NO_AUTO_VALUE_ON_ZERO', SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- ================================================================
-- 1. 创建数据库
-- ================================================================
DROP DATABASE IF EXISTS `ai_agent_station`;
CREATE DATABASE `ai_agent_station` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `ai_agent_station`;

-- ================================================================
-- 2. 表结构定义
-- ================================================================

-- 2.1 AI智能体配置表
DROP TABLE IF EXISTS `ai_agent`;
CREATE TABLE `ai_agent` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `agent_id` varchar(64) NOT NULL COMMENT '智能体ID',
    `agent_name` varchar(50) NOT NULL COMMENT '智能体名称',
    `description` varchar(255) DEFAULT NULL COMMENT '描述',
    `channel` varchar(32) DEFAULT NULL COMMENT '渠道类型(agent，chat_stream)',
    `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_agent_id` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI智能体配置表';

-- 2.2 AI客户端配置表
DROP TABLE IF EXISTS `ai_client`;
CREATE TABLE `ai_client` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `client_id` varchar(64) NOT NULL COMMENT '客户端ID',
    `client_name` varchar(100) NOT NULL COMMENT '客户端名称',
    `description` varchar(1024) DEFAULT NULL COMMENT '描述',
    `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_client_id` (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI客户端配置表';

-- 2.3 OpenAI API配置表
DROP TABLE IF EXISTS `ai_client_api`;
CREATE TABLE `ai_client_api` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `api_id` varchar(64) NOT NULL COMMENT '全局唯一配置ID',
    `base_url` varchar(255) NOT NULL COMMENT 'API基础URL',
    `api_key` varchar(255) NOT NULL COMMENT 'API密钥',
    `completions_path` varchar(255) NOT NULL COMMENT '补全API路径',
    `embeddings_path` varchar(255) NOT NULL COMMENT '嵌入API路径',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_api_id` (`api_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OpenAI API配置表';

-- 2.4 聊天模型配置表
DROP TABLE IF EXISTS `ai_client_model`;
CREATE TABLE `ai_client_model` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
    `model_id` varchar(64) NOT NULL COMMENT '全局唯一模型ID',
    `api_id` varchar(64) NOT NULL COMMENT '关联的API配置ID',
    `model_name` varchar(64) NOT NULL COMMENT '模型名称',
    `model_type` varchar(32) NOT NULL COMMENT '模型类型：openai、deepseek、claude',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_model_id` (`model_id`),
    KEY `idx_api_config_id` (`api_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天模型配置表';

-- 2.5 MCP客户端配置表
DROP TABLE IF EXISTS `ai_client_tool_mcp`;
CREATE TABLE `ai_client_tool_mcp` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `mcp_id` varchar(64) NOT NULL COMMENT 'MCP ID',
    `mcp_name` varchar(50) NOT NULL COMMENT 'MCP名称',
    `transport_type` varchar(20) NOT NULL COMMENT '传输类型(sse/stdio)',
    `transport_config` varchar(2048) DEFAULT NULL COMMENT '传输配置(JSON格式)',
    `request_timeout` int DEFAULT '180' COMMENT '请求超时时间(秒)',
    `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_mcp_id` (`mcp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='MCP客户端配置表';

-- 2.6 系统提示词配置表
DROP TABLE IF EXISTS `ai_client_system_prompt`;
CREATE TABLE `ai_client_system_prompt` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `prompt_id` varchar(64) NOT NULL COMMENT '提示词ID',
    `prompt_name` varchar(50) NOT NULL COMMENT '提示词名称',
    `prompt_content` text NOT NULL COMMENT '提示词内容',
    `description` varchar(1024) DEFAULT NULL COMMENT '描述',
    `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_prompt_id` (`prompt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统提示词配置表';

-- 2.7 顾问配置表
DROP TABLE IF EXISTS `ai_client_advisor`;
CREATE TABLE `ai_client_advisor` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `advisor_id` varchar(64) NOT NULL COMMENT '顾问ID',
    `advisor_name` varchar(50) NOT NULL COMMENT '顾问名称',
    `advisor_type` varchar(32) NOT NULL COMMENT '顾问类型(CHAT_MEMORY,RAG_ANSWER,TECHNICAL_EXPERT)',
    `ext_param` varchar(1024) DEFAULT NULL COMMENT '扩展参数(JSON格式)',
    `order_num` int DEFAULT '0' COMMENT '排序号',
    `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_advisor_id` (`advisor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='顾问配置表';

-- 2.8 AI客户端统一关联配置表
DROP TABLE IF EXISTS `ai_client_config`;
CREATE TABLE `ai_client_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `source_type` varchar(32) NOT NULL COMMENT '源类型（model、client）',
    `source_id` varchar(64) NOT NULL COMMENT '源ID（如 chatModelId、chatClientId 等）',
    `target_type` varchar(32) NOT NULL COMMENT '目标类型（api、model、mcp、prompt、advisor）',
    `target_id` varchar(64) NOT NULL COMMENT '目标ID（如 apiId、modelId、mcpId、promptId、advisorId 等）',
    `ext_param` varchar(1024) DEFAULT NULL COMMENT '扩展参数（JSON格式）',
    `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用,1:启用)',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_source_id` (`source_id`),
    KEY `idx_target_id` (`target_id`),
    KEY `idx_source_target` (`source_type`, `source_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI客户端统一关联配置表';

-- 2.9 AI智能体流程配置表
DROP TABLE IF EXISTS `ai_agent_flow_config`;
CREATE TABLE `ai_agent_flow_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `agent_id` varchar(64) NOT NULL COMMENT '智能体ID',
    `client_id` varchar(64) NOT NULL COMMENT '客户端ID',
    `client_name` varchar(64) DEFAULT NULL COMMENT '客户端名称',
    `client_type` varchar(64) DEFAULT NULL COMMENT '客户端类型',
    `sequence` int NOT NULL COMMENT '序列号(执行顺序)',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_agent_client_seq` (`agent_id`,`client_id`,`sequence`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能体-客户端关联表';

-- 2.10 RAG订单表
DROP TABLE IF EXISTS `ai_client_rag_order`;
CREATE TABLE `ai_client_rag_order` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` varchar(64) NOT NULL COMMENT '订单ID',
    `user_id` varchar(64) NOT NULL COMMENT '用户ID',
    `product_name` varchar(128) NOT NULL COMMENT '产品名称',
    `order_amount` decimal(8,2) NOT NULL COMMENT '订单金额',
    `order_status` varchar(32) NOT NULL COMMENT '订单状态',
    `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='RAG订单表';

-- ================================================================
-- 3. 基础测试数据插入
-- ================================================================

-- 3.1 AI智能体配置
INSERT INTO `ai_agent` (`agent_id`, `agent_name`, `description`, `channel`, `status`, `create_time`, `update_time`) VALUES
('1', 'AI Agent文章发布助手', 'AI Agent智能体，可以生成技术文章并发布到CSDN平台，同时发送微信公众号通知', 'agent', 1, NOW(), NOW()),
('2', 'AI智能对话体', '基于MCP协议的智能对话体，具备文件操作、搜索等工具能力', 'chat_stream', 1, NOW(), NOW()),
('3', 'AutoAgent智能对话体', '自动分析和执行任务的智能体，支持多轮对话和复杂任务处理', 'agent', 1, NOW(), NOW());

-- 3.2 AI客户端配置
INSERT INTO `ai_client` (`client_id`, `client_name`, `description`, `status`, `create_time`, `update_time`) VALUES 
('3001', 'YunWu AI客户端', '云雾AI GPT-4o客户端，支持完整的AI Agent功能，包括文件操作、文章发布等', 1, NOW(), NOW()),
('3002', 'OpenAI官方客户端', 'OpenAI官方API客户端，用于对比测试和高级功能验证', 1, NOW(), NOW()),
('3101', '任务分析和状态判断', '专业的任务分析师，负责分析任务状态和制定执行策略', 1, NOW(), NOW()),
('3102', '具体任务执行', '精准任务执行器，负责严格按照策略执行具体任务', 1, NOW(), NOW()),
('3103', '质量检查和优化', '专业的质量监督员，负责监督和评估执行质量', 1, NOW(), NOW()),
('3104', '智能响应助手', '智能响应助手，负责响应式处理和最终结果输出', 1, NOW(), NOW());

-- 3.3 API配置
INSERT INTO `ai_client_api` (`api_id`, `base_url`, `api_key`, `completions_path`, `embeddings_path`, `status`, `create_time`, `update_time`) VALUES 
('1001', 'https://yunwu.ai', 'your-api-key', 'v1/chat/completions', 'v1/embeddings', 1, NOW(), NOW()),
('1002', 'https://api.openai.com', 'sk-test-openai-key-replace-with-real', '/v1/chat/completions', '/v1/embeddings', 1, NOW(), NOW());

-- 3.4 模型配置
INSERT INTO `ai_client_model` (`model_id`, `api_id`, `model_name`, `model_type`, `status`, `create_time`, `update_time`) VALUES 
('2001', '1001', 'gpt-4o', 'openai', 1, NOW(), NOW()),
('2002', '1001', 'gpt-4.1', 'openai', 1, NOW(), NOW()),
('2003', '1002', 'gpt-4o-2024-08-06:free', 'openai', 1, NOW(), NOW()),
('2004', '1002', 'gpt-4o-2024-11-20', 'openai', 1, NOW(), NOW());

-- 3.5 MCP工具配置 (注意：使用正确的npx完整路径和JSON格式)
INSERT INTO `ai_client_tool_mcp` (`mcp_id`, `mcp_name`, `transport_type`, `transport_config`, `request_timeout`, `status`, `create_time`, `update_time`) VALUES 
('5001', 'CSDN文章发布工具', 'sse', '{"baseUri":"http://127.0.0.1:8102","sseEndpoint":"/sse","description":"用于发布文章到CSDN平台"}', 180, 1, NOW(), NOW()),
('5002', '微信公众号通知工具', 'sse', '{"baseUri":"http://127.0.0.1:8101","sseEndpoint":"/sse","description":"用于发送微信公众号消息通知"}', 180, 1, NOW(), NOW()),
('5003', 'FileSystem文件操作工具', 'stdio', '{"stdio":{"filesystem":{"command":"D:/Develop/nodeJs/npx.cmd","args":["-y","@modelcontextprotocol/server-filesystem","D:/Develop/Projects/xfg/ai-agent-station-study-3-3-agent-case/ai-agent-station-study-3-3-agent-case","D:/Develop/Projects/xfg/ai-agent-station-study-3-3-agent-case/ai-agent-station-study-3-3-agent-case"],"env":{"NODE_ENV":"production"}}}}', 180, 1, NOW(), NOW()),
('5004', 'Google搜索工具', 'stdio', '{"stdio":{"g-search":{"command":"D:/Develop/nodeJs/npx.cmd","args":["-y","g-search-mcp"],"env":{"NODE_ENV":"production"}}}}', 180, 1, NOW(), NOW()),
('5005', '高德地图工具', 'sse', '{"baseUri":"https://mcp.amap.com","sseEndpoint":"/sse?key=801aabf79ed055c2ff78603cfe851787"}', 180, 1, NOW(), NOW());

-- 3.6 系统提示词配置
INSERT INTO `ai_client_system_prompt` (`prompt_id`, `prompt_name`, `prompt_content`, `description`, `status`, `create_time`, `update_time`) VALUES 
('4001', 'AI Agent智能体核心提示词', 
'你是一个 AI Agent 智能体，可以根据用户输入信息生成文章，并发送到 CSDN 平台以及完成微信公众号消息通知，今天是 {current_date}。

你擅长使用Planning模式，帮助用户生成质量更高的文章。

你的规划应该包括以下几个方面：
1. 分析用户输入的内容，生成技术文章。
2. 提取，文章标题（需要含带技术点）、文章内容、文章标签（多个用英文逗号隔开）、文章简述（100字）将以上内容发布文章到CSDN
3. 获取发送到 CSDN 文章的 URL 地址。
4. 微信公众号消息通知，平台：CSDN、主题：为文章标题、描述：为文章简述、跳转地址：从发布文章到CSDN获取 URL 地址', 
'AI Agent核心智能体系统提示词，用于文章生成和发布', 1, NOW(), NOW()),

('4002', '技术助手提示词', 
'你是一个专业的技术助手，擅长解答各种编程和技术问题。请用专业、准确、易懂的方式回答用户的技术问题。', 
'通用技术助手提示词', 1, NOW(), NOW()),

('4101', '任务分析师提示词',
'# 角色
你是一个专业的任务分析师，名叫 AutoAgent Task Analyzer。
# 核心职责
你负责分析任务的当前状态、执行历史和下一步行动计划：
1. **状态分析**: 深度分析当前任务完成情况和执行历史
2. **进度评估**: 评估任务完成进度和质量
3. **策略制定**: 制定下一步最优执行策略
4. **完成判断**: 准确判断任务是否已完成
# 分析原则
- **全面性**: 综合考虑所有执行历史和当前状态
- **准确性**: 准确评估任务完成度和质量
- **前瞻性**: 预测可能的问题和最优路径
- **效率性**: 优化执行路径，避免重复工作
# 输出格式
**任务状态分析:**
[当前任务完成情况的详细分析]
**执行历史评估:**
[对已完成工作的质量和效果评估]
**下一步策略:**
[具体的下一步执行计划和策略]
**完成度评估:** [0-100]%
**任务状态:** [CONTINUE/COMPLETED]',
'AutoAgent任务分析师系统提示词', 1, NOW(), NOW()),

('4102', '精准执行器提示词',
'# 角色
你是一个精准任务执行器，名叫 AutoAgent Precision Executor。
# 核心能力
你专注于精准执行具体的任务步骤：
1. **精准执行**: 严格按照分析师的策略执行任务
2. **工具使用**: 熟练使用各种工具完成复杂操作
3. **质量控制**: 确保每一步执行的准确性和完整性
4. **结果记录**: 详细记录执行过程和结果
# 执行原则
- **专注性**: 专注于当前分配的具体任务
- **精准性**: 确保执行结果的准确性和质量
- **完整性**: 完整执行所有必要的步骤
- **可追溯性**: 详细记录执行过程便于后续分析
# 输出格式
**执行目标:**
[本轮要执行的具体目标]
**执行过程:**
[详细的执行步骤和使用的工具]
**执行结果:**
[执行的具体结果和获得的信息]
**质量检查:**
[对执行结果的质量评估]',
'AutoAgent精准执行器系统提示词', 1, NOW(), NOW()),

('4103', '质量监督员提示词',
'# 角色
你是一个专业的质量监督员，名叫 AutoAgent Quality Supervisor。
# 核心职责
你负责监督和评估执行质量：
1. **质量评估**: 评估执行结果的准确性和完整性
2. **问题识别**: 识别执行过程中的问题和不足
3. **改进建议**: 提供具体的改进建议和优化方案
4. **标准制定**: 制定质量标准和评估指标
# 评估标准
- **准确性**: 结果是否准确无误
- **完整性**: 是否遗漏重要信息
- **相关性**: 是否符合用户需求
- **可用性**: 结果是否实用有效
# 输出格式
**质量评估:**
[对执行结果的详细质量评估]
**问题识别:**
[发现的问题和不足之处]
**改进建议:**
[具体的改进建议和优化方案]
**质量评分:** [0-100]分
**是否通过:** [PASS/FAIL/OPTIMIZE]',
'AutoAgent质量监督员系统提示词', 1, NOW(), NOW()),

('4104', '智能响应助手提示词',
'# 角色
你是一个智能响应助手，名叫 AutoAgent Response Assistant。
# 核心职责
你负责最终结果的整理和输出：
1. **结果整理**: 整理前面步骤的执行结果
2. **格式优化**: 优化输出格式使其更易阅读
3. **内容总结**: 提供清晰的总结和要点
4. **用户友好**: 确保输出对用户友好和有用
# 输出原则
- **清晰性**: 结果表达清晰明了
- **完整性**: 包含所有重要信息
- **实用性**: 对用户具有实际价值
- **格式化**: 使用合适的格式展示
# 输出格式
**执行总结:**
[对整个执行过程的总结]
**主要成果:**
[列出主要的执行成果]
**详细结果:**
[详细的执行结果内容]
**建议和提醒:**
[对用户的建议和提醒事项]',
'AutoAgent智能响应助手系统提示词', 1, NOW(), NOW());

-- 3.7 顾问配置
INSERT INTO `ai_client_advisor` (`advisor_id`, `advisor_name`, `advisor_type`, `ext_param`, `order_num`, `status`, `create_time`, `update_time`) VALUES 
('6001', '对话记忆顾问', 'CHAT_MEMORY', '{"maxMessages":50,"windowSize":10}', 1, 1, NOW(), NOW()),
('6002', 'RAG检索顾问', 'RAG_ANSWER', '{"topK":5,"filterExpression":"","threshold":0.7}', 2, 1, NOW(), NOW()),
('6003', '技术专家顾问', 'TECHNICAL_EXPERT', '{"expertise":["Java","Spring","AI","数据库"],"style":"professional"}', 3, 1, NOW(), NOW());

-- 3.8 AI智能体流程配置
-- 清理可能存在的重复配置
DELETE FROM `ai_agent_flow_config` WHERE agent_id = '3' AND client_id IN ('3101', '3102', '3103', '3104');

INSERT INTO `ai_agent_flow_config` (`agent_id`, `client_id`, `client_name`, `client_type`, `sequence`, `create_time`) VALUES
('1', '3001', '通用对话客户端', 'DEFAULT', 1, NOW()),
('3', '3101', '任务分析和状态判断', 'TASK_ANALYZER_CLIENT', 1, NOW()),
('3', '3102', '具体任务执行', 'PRECISION_EXECUTOR_CLIENT', 2, NOW()),
('3', '3103', '质量检查和优化', 'QUALITY_SUPERVISOR_CLIENT', 3, NOW()),
('3', '3104', '智能响应助手', 'RESPONSE_ASSISTANT', 4, NOW());

-- 3.9 关联配置 (确保关联关系正确，使用 mcp 而不是 tool_mcp)
-- 清理可能存在的冲突配置 - 扩展清理范围
DELETE FROM `ai_client_config` WHERE source_type = 'client' AND source_id IN ('3101', '3102', '3103', '3104');
DELETE FROM `ai_client_config` WHERE source_type = 'model' AND source_id = '2001' AND target_type = 'mcp';

INSERT INTO `ai_client_config` (`source_type`, `source_id`, `target_type`, `target_id`, `ext_param`, `status`, `create_time`, `update_time`) VALUES 
-- 客户端3001的完整配置
('client', '3001', 'api', '1001', '{"priority":1}', 1, NOW(), NOW()),
('client', '3001', 'model', '2001', '{"priority":1}', 1, NOW(), NOW()),
('client', '3001', 'mcp', '5003', '{"order":1,"description":"文件操作工具"}', 1, NOW(), NOW()),
('client', '3001', 'prompt', '4001', '{"primary":true}', 1, NOW(), NOW()),
('client', '3001', 'advisor', '6001', '{"order":1}', 1, NOW(), NOW()),
('client', '3001', 'advisor', '6003', '{"order":2}', 1, NOW(), NOW()),

-- 客户端3002的完整配置  
('client', '3002', 'api', '1002', '{"priority":1}', 1, NOW(), NOW()),
('client', '3002', 'model', '2003', '{"priority":1}', 1, NOW(), NOW()),
('client', '3002', 'mcp', '5004', '{"order":1,"description":"搜索工具"}', 1, NOW(), NOW()),
('client', '3002', 'prompt', '4002', '{"primary":true}', 1, NOW(), NOW()),
('client', '3002', 'advisor', '6001', '{"order":1}', 1, NOW(), NOW()),

-- AutoAgent专用客户端配置 (3101-3103)
('client', '3101', 'api', '1001', '{"priority":1}', 1, NOW(), NOW()),
('client', '3101', 'model', '2001', '{"priority":1}', 1, NOW(), NOW()),
('client', '3101', 'prompt', '4101', '{"primary":true}', 1, NOW(), NOW()),
('client', '3101', 'advisor', '6001', '{"order":1}', 1, NOW(), NOW()),
('client', '3101', 'mcp', '5003', '{"order":1,"description":"文件操作工具"}', 1, NOW(), NOW()),

('client', '3102', 'api', '1001', '{"priority":1}', 1, NOW(), NOW()),
('client', '3102', 'model', '2001', '{"priority":1}', 1, NOW(), NOW()),
('client', '3102', 'prompt', '4102', '{"primary":true}', 1, NOW(), NOW()),
('client', '3102', 'advisor', '6001', '{"order":1}', 1, NOW(), NOW()),
('client', '3102', 'mcp', '5003', '{"order":1,"description":"文件操作工具"}', 1, NOW(), NOW()),

('client', '3103', 'api', '1001', '{"priority":1}', 1, NOW(), NOW()),
('client', '3103', 'model', '2001', '{"priority":1}', 1, NOW(), NOW()),
('client', '3103', 'prompt', '4103', '{"primary":true}', 1, NOW(), NOW()),
('client', '3103', 'advisor', '6001', '{"order":1}', 1, NOW(), NOW()),
('client', '3103', 'mcp', '5003', '{"order":1,"description":"文件操作工具"}', 1, NOW(), NOW()),

-- 客户端3104的完整配置
('client', '3104', 'api', '1001', '{"priority":1}', 1, NOW(), NOW()),
('client', '3104', 'model', '2001', '{"priority":1}', 1, NOW(), NOW()),
('client', '3104', 'prompt', '4104', '{"primary":true}', 1, NOW(), NOW()),
('client', '3104', 'advisor', '6001', '{"order":1}', 1, NOW(), NOW()),

-- 模型关联MCP工具 (确保模型2001关联到正确的MCP工具)
('model', '2001', 'mcp', '5003', '{"order":1,"description":"文件操作工具"}', 1, NOW(), NOW()),
('model', '2002', 'mcp', '5004', '{"order":1,"description":"搜索工具"}', 1, NOW(), NOW());

-- 3.10 测试用RAG订单数据
INSERT INTO `ai_client_rag_order` (`order_id`, `user_id`, `product_name`, `order_amount`, `order_status`, `pay_time`, `create_time`, `update_time`) VALUES 
('ORDER_001', 'USER_001', 'AI Agent专业版', 299.00, 'PAID', '2025-08-20 10:30:00', NOW(), NOW()),
('ORDER_002', 'USER_002', 'AI Agent标准版', 199.00, 'PAID', '2025-08-21 14:20:00', NOW(), NOW()),
('ORDER_003', 'USER_003', 'AI Agent基础版', 99.00, 'PENDING', NULL, NOW(), NOW());

-- ================================================================
-- 4. 数据验证查询
-- ================================================================

-- 验证客户端配置
SELECT 'ai_client验证' as table_name, COUNT(*) as count FROM ai_client;

-- 验证API配置
SELECT 'ai_client_api验证' as table_name, COUNT(*) as count FROM ai_client_api;

-- 验证模型配置
SELECT 'ai_client_model验证' as table_name, COUNT(*) as count FROM ai_client_model;

-- 验证MCP工具配置
SELECT 'ai_client_tool_mcp验证' as table_name, COUNT(*) as count FROM ai_client_tool_mcp;

-- 验证关联配置
SELECT 'ai_client_config验证' as table_name, 
       target_type, COUNT(*) as count 
FROM ai_client_config 
GROUP BY target_type;

-- 验证智能体流程配置
SELECT 'ai_agent_flow_config验证' as table_name, COUNT(*) as count FROM ai_agent_flow_config;

-- 验证AutoAgent智能体3的流程配置
SELECT 
    'AutoAgent流程配置验证' as description,
    afc.agent_id,
    afc.client_id,
    afc.client_name,
    afc.client_type,
    afc.sequence,
    ac.client_name as actual_client_name
FROM ai_agent_flow_config afc 
LEFT JOIN ai_client ac ON afc.client_id = ac.client_id
WHERE afc.agent_id = '3'
ORDER BY afc.sequence;

-- 验证客户端3001的完整配置链
SELECT 
    '客户端3001配置验证' as description,
    cc.source_type,
    cc.target_type,
    cc.target_id,
    CASE 
        WHEN cc.target_type = 'api' THEN (SELECT base_url FROM ai_client_api WHERE api_id = cc.target_id)
        WHEN cc.target_type = 'model' THEN (SELECT model_name FROM ai_client_model WHERE model_id = cc.target_id)
        WHEN cc.target_type = 'mcp' THEN (SELECT mcp_name FROM ai_client_tool_mcp WHERE mcp_id = cc.target_id)
        WHEN cc.target_type = 'prompt' THEN (SELECT prompt_name FROM ai_client_system_prompt WHERE prompt_id = cc.target_id)
        WHEN cc.target_type = 'advisor' THEN (SELECT advisor_name FROM ai_client_advisor WHERE advisor_id = cc.target_id)
        ELSE 'Unknown'
    END as target_name,
    cc.status
FROM ai_client_config cc 
WHERE cc.source_type = 'client' AND cc.source_id = '3001'
ORDER BY cc.target_type;

-- 验证AutoAgent客户端配置详情 (重点验证)
SELECT 
    'AutoAgent客户端配置详细验证' as description,
    cc.source_id as client_id,
    ac.client_name,
    cc.target_type,
    cc.target_id,
    CASE 
        WHEN cc.target_type = 'api' THEN (SELECT CONCAT(base_url, ' (', api_id, ')') FROM ai_client_api WHERE api_id = cc.target_id)
        WHEN cc.target_type = 'model' THEN (SELECT CONCAT(model_name, ' (', model_id, ')') FROM ai_client_model WHERE model_id = cc.target_id)
        WHEN cc.target_type = 'mcp' THEN (SELECT CONCAT(mcp_name, ' (', mcp_id, ')') FROM ai_client_tool_mcp WHERE mcp_id = cc.target_id)
        WHEN cc.target_type = 'prompt' THEN (SELECT CONCAT(prompt_name, ' (', prompt_id, ')') FROM ai_client_system_prompt WHERE prompt_id = cc.target_id)
        WHEN cc.target_type = 'advisor' THEN (SELECT CONCAT(advisor_name, ' (', advisor_id, ')') FROM ai_client_advisor WHERE advisor_id = cc.target_id)
        ELSE 'Unknown'
    END as target_details,
    cc.ext_param,
    cc.status,
    CASE WHEN cc.status = 1 THEN '✅启用' ELSE '❌禁用' END as status_text
FROM ai_client_config cc 
LEFT JOIN ai_client ac ON cc.source_id = ac.client_id
WHERE cc.source_type = 'client' AND cc.source_id IN ('3101', '3102', '3103')
ORDER BY cc.source_id, 
    CASE cc.target_type 
        WHEN 'api' THEN 1 
        WHEN 'model' THEN 2 
        WHEN 'prompt' THEN 3 
        WHEN 'advisor' THEN 4 
        WHEN 'mcp' THEN 5 
        ELSE 6 
    END;

-- 验证MCP配置中的npx路径
SELECT 
    'MCP配置验证' as description,
    mcp_id,
    mcp_name,
    transport_type,
    CASE 
        WHEN transport_config LIKE '%D:/Develop/nodeJs/npx.cmd%' THEN 'NPX路径正确' 
        WHEN transport_config LIKE '%npx%' AND transport_config NOT LIKE '%D:/Develop/nodeJs/npx.cmd%' THEN 'NPX路径需要修正'
        ELSE 'SSE类型或无NPX'
    END as path_check,
    status
FROM ai_client_tool_mcp 
ORDER BY mcp_id;

-- 验证关键配置完整性检查
SELECT 
    'AutoAgent配置完整性检查' as description,
    client_ids.client_id,
    CASE WHEN api_count > 0 THEN '✅' ELSE '❌' END as has_api,
    CASE WHEN model_count > 0 THEN '✅' ELSE '❌' END as has_model,
    CASE WHEN prompt_count > 0 THEN '✅' ELSE '❌' END as has_prompt,
    CASE WHEN advisor_count > 0 THEN '✅' ELSE '❌' END as has_advisor,
    CASE WHEN mcp_count > 0 THEN '✅' ELSE '❌' END as has_mcp,
    CASE 
        WHEN api_count > 0 AND model_count > 0 AND prompt_count > 0 AND advisor_count > 0 THEN '✅配置完整' 
        ELSE '❌配置不完整'
    END as config_status
FROM (
    SELECT '3101' as client_id UNION ALL
    SELECT '3102' as client_id UNION ALL
    SELECT '3103' as client_id
) client_ids
LEFT JOIN (
    SELECT source_id, 
           SUM(CASE WHEN target_type = 'api' AND status = 1 THEN 1 ELSE 0 END) as api_count,
           SUM(CASE WHEN target_type = 'model' AND status = 1 THEN 1 ELSE 0 END) as model_count,
           SUM(CASE WHEN target_type = 'prompt' AND status = 1 THEN 1 ELSE 0 END) as prompt_count,
           SUM(CASE WHEN target_type = 'advisor' AND status = 1 THEN 1 ELSE 0 END) as advisor_count,
           SUM(CASE WHEN target_type = 'mcp' AND status = 1 THEN 1 ELSE 0 END) as mcp_count
    FROM ai_client_config 
    WHERE source_type = 'client' AND source_id IN ('3101', '3102', '3103')
    GROUP BY source_id
) config_counts ON client_ids.client_id = config_counts.source_id
ORDER BY client_ids.client_id;

-- ================================================================
-- 5. 完成提示
-- ================================================================

SELECT '=== AI Agent Station 数据库初始化完成 ===' as status,
       'DATABASE: ai_agent_station' as database_name,
       'TABLES: 9个核心表' as table_count,
       'TEST_DATA: 完整测试数据已加载' as data_status,
       'READY: 可以开始运行AI Agent测试' as ready_status;

-- 恢复SQL设置
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */; 