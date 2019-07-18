-- 注意：以下脚本仅在开发时使用！
--
-- 清空组件市场相关的所有表
 
truncate table component_repo_publish_task;
truncate table component_repo;
truncate table component_repo_version;
truncate table api_changelog;
truncate table api_repo;
truncate table api_repo_version;
truncate table api_component;
truncate table api_component_attr;
truncate table api_component_attr_val_opt;
truncate table api_component_attr_fun_arg;