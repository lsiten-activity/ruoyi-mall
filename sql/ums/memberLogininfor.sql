-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('会员登录记录', '3', '1', 'memberLogininfor', 'ums/memberLogininfor/index', 1, 0, 'C', '0', '0', 'ums:memberLogininfor:list', '#', 1, sysdate(), '', null, '会员登录记录菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('会员登录记录查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'ums:memberLogininfor:query',        '#', 1, sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('会员登录记录新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'ums:memberLogininfor:add',          '#', 1, sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('会员登录记录修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'ums:memberLogininfor:edit',         '#', 1, sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('会员登录记录删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'ums:memberLogininfor:remove',       '#', 1, sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('会员登录记录导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'ums:memberLogininfor:export',       '#', 1, sysdate(), '', null, '');
