use oa;
if exists ( select *
 from  sysobjects
 where name = 'OA_USER'
 and type = 'U')
 drop table OA_USER
go

CREATE TABLE OA_USER
(
  USER_ID           INT IDENTITY PRIMARY KEY,
  USER_NAME         NVARCHAR(200) UNIQUE NOT NULL ,
  GROUP_ID          INT DEFAULT 0,
  USER_MOBILE       NVARCHAR(20) UNIQUE,
  USER_EMAIL        NVARCHAR(200),
  USER_PWD          NVARCHAR(200),
  ENTERPRISE_ID     INT DEFAULT 0,,
  USER_COMPANY      NVARCHAR(200),
  USER_DEPARTMENT   NVARCHAR(200),
  USER_POSITION     NVARCHAR(200),
  USER_ADDRESS      NVARCHAR(200),
  USER_POSTCODE     NVARCHAR(200),
  USER_WEIXIN       NVARCHAR(100),
  PARENT_ID         INT DEFAULT 0,
  USER_STATUS       INT DEFAULT 0,
  USER_CREATETIME   DATETIME
)
GO
/* 表注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'用户表', N'user', N'dbo', N'table', N'OA_USER', NULL, NULL;
/* 字段注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'用户编号', N'user', N'dbo', N'table', N'OA_USER', N'column', N'USER_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'用户名称', N'user', N'dbo', N'table', N'OA_USER', N'column', N'USER_NAME';
EXECUTE sp_addextendedproperty N'MS_Description', N'用户组编号', N'user', N'dbo', N'table', N'OA_USER', N'column', N'GROUP_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'用户手机号码', N'user', N'dbo', N'table', N'OA_USER', N'column', N'USER_MOBILE';
EXECUTE sp_addextendedproperty N'MS_Description', N'用户邮箱地址', N'user', N'dbo', N'table', N'OA_USER', N'column', N'USER_EMAIL';
EXECUTE sp_addextendedproperty N'MS_Description', N'用户密码', N'user', N'dbo', N'table', N'OA_USER', N'column', N'USER_PWD';
EXECUTE sp_addextendedproperty N'MS_Description', N'用户所属公司', N'user', N'dbo', N'table', N'OA_USER', N'column', N'USER_COMPANY';
EXECUTE sp_addextendedproperty N'MS_Description', N'用户部门', N'user', N'dbo', N'table', N'OA_USER', N'column', N'USER_DEPARTMENT';
EXECUTE sp_addextendedproperty N'MS_Description', N'用户岗位', N'user', N'dbo', N'table', N'OA_USER', N'column', N'USER_POSITION';
EXECUTE sp_addextendedproperty N'MS_Description', N'用户住址', N'user', N'dbo', N'table', N'OA_USER', N'column', N'USER_ADDRESS';
EXECUTE sp_addextendedproperty N'MS_Description', N'用户邮编', N'user', N'dbo', N'table', N'OA_USER', N'column', N'USER_POSTCODE';
EXECUTE sp_addextendedproperty N'MS_Description', N'用户微信号', N'user', N'dbo', N'table', N'OA_USER', N'column', N'USER_WEIXIN';
EXECUTE sp_addextendedproperty N'MS_Description', N'用户状态', N'user', N'dbo', N'table', N'OA_USER', N'column', N'USER_STATUS';
EXECUTE sp_addextendedproperty N'MS_Description', N'用户创建时间', N'user', N'dbo', N'table', N'OA_USER', N'column', N'USER_CREATETIME';

insert OA_USER (USER_NAME,USER_PWD,USER_STATUS,USER_CREATETIME) values ('admin','202CB962AC59075B964B07152D234B70',1,'2018-04-23 11:06:21.517');

if exists ( select *
 from  sysobjects
 where name = 'OA_CONTRACT_TEMPLATE'
 and type = 'U')
 drop table OA_CONTRACT_TEMPLATE
go
CREATE TABLE OA_CONTRACT_TEMPLATE
(
  TEMPLATE_ID         INT IDENTITY PRIMARY KEY,
  TEMPLATE_NAME       NVARCHAR(255) UNIQUE ,
  USER_ID             INT,
  TEMPLATE_STATUS     INT,
  TEMPLATE_HTML       TEXT,
  TEMPLATE_DES        NVARCHAR(255),
  TEMPLATE_CREATETIME DATETIME
)
GO
/* 表注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'合同模板表', N'user', N'dbo', N'table', N'OA_CONTRACT_TEMPLATE', NULL, NULL;
/* 字段注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'模板编号', N'user', N'dbo', N'table', N'OA_CONTRACT_TEMPLATE', N'column', N'TEMPLATE_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'模板名称', N'user', N'dbo', N'table', N'OA_CONTRACT_TEMPLATE', N'column', N'TEMPLATE_NAME';
EXECUTE sp_addextendedproperty N'MS_Description', N'创建模板', N'user', N'dbo', N'table', N'OA_CONTRACT_TEMPLATE', N'column', N'USER_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'模板启用状态', N'user', N'dbo', N'table', N'OA_CONTRACT_TEMPLATE', N'column', N'TEMPLATE_STATUS';
EXECUTE sp_addextendedproperty N'MS_Description', N'模板内容', N'user', N'dbo', N'table', N'OA_CONTRACT_TEMPLATE', N'column', N'TEMPLATE_HTML';
EXECUTE sp_addextendedproperty N'MS_Description', N'模板描述信息', N'user', N'dbo', N'table', N'OA_CONTRACT_TEMPLATE', N'column', N'TEMPLATE_DES';
EXECUTE sp_addextendedproperty N'MS_Description', N'模板创建时间', N'user', N'dbo', N'table', N'OA_CONTRACT_TEMPLATE', N'column', N'TEMPLATE_CREATETIME';

INSERT INTO OA_CONTRACT_TEMPLATE(TEMPLATE_NAME,USER_ID,TEMPLATE_STATUS,TEMPLATE_HTML,TEMPLATE_CREATETIME) VALUES ('自定义合同',1,1,'<DIV style="margin-top: 25px;">
                <div class="row" id="customFile">
                    <div class="col-md-6">
                        <input id="file" type="file" accept=".doc,.docx" name="file" class="" style="" value="选择word文档">
                    </div>
                    <div class="col-md-6">
                        　　<input type="submit" onclick="uploadFile();" name="submit" id="submit" ng-hide="loginUserMenuMap[currentView]" value="上传" class="btn btn-primary btn-lg" >  <span id="sendStatus"></span>
                    </div>
                </div>
		<div style="margin-top: 25px;margin-bottom: 20px;" id="download">
		</div>
</DIV>','2018-04-23 11:06:21.517');

if exists ( select *
 from  sysobjects
 where name = 'OA_DEPLOYMENT_TEMPLATE_RELATION'
 and type = 'U')
 drop table OA_DEPLOYMENT_TEMPLATE_RELATION
go
CREATE TABLE OA_DEPLOYMENT_TEMPLATE_RELATION
(
  RELATION_ID           INT IDENTITY PRIMARY KEY,
  RELATION_DEPLOYMENTID NVARCHAR(255),
  RELATION_TEMPLATEID   INT,
  RELATION_CREATETIME   DATETIME
)
GO

/* 表注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'部署流程与合同模板关联表', N'user', N'dbo', N'table', N'OA_DEPLOYMENT_TEMPLATE_RELATION', NULL, NULL;
/* 字段注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'关联编号', N'user', N'dbo', N'table', N'OA_DEPLOYMENT_TEMPLATE_RELATION', N'column', N'RELATION_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'部署流程编号', N'user', N'dbo', N'table', N'OA_DEPLOYMENT_TEMPLATE_RELATION', N'column', N'RELATION_DEPLOYMENTID';
EXECUTE sp_addextendedproperty N'MS_Description', N'合同模板编号', N'user', N'dbo', N'table', N'OA_DEPLOYMENT_TEMPLATE_RELATION', N'column', N'RELATION_TEMPLATEID';
EXECUTE sp_addextendedproperty N'MS_Description', N'关联创建时间', N'user', N'dbo', N'table', N'OA_DEPLOYMENT_TEMPLATE_RELATION', N'column', N'RELATION_CREATETIME';

if exists ( select *
 from  sysobjects
 where name = 'OA_FORM_PROPERTIES'
 and type = 'U')
 drop table OA_FORM_PROPERTIES
go
CREATE TABLE OA_FORM_PROPERTIES
(
  PROPERTIES_ID INT IDENTITY PRIMARY KEY,
  TEMPLATE_ID   INT,
  FIELD_NAME    NVARCHAR(255),
  FIELD_MD5     NVARCHAR(255) NOT NULL,
  FIELD_TYPE    NVARCHAR(255),
  FIELD_VALID   NVARCHAR(255),
  STATUS        INT,
  CREATE_TIME   DATETIME
)
GO

/* 表注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'合同模板表单项', N'user', N'dbo', N'table', N'OA_FORM_PROPERTIES', NULL, NULL;
/* 字段注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'表单项编号', N'user', N'dbo', N'table', N'OA_FORM_PROPERTIES', N'column', N'PROPERTIES_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'合同模板编号', N'user', N'dbo', N'table', N'OA_FORM_PROPERTIES', N'column', N'TEMPLATE_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'表单项名称', N'user', N'dbo', N'table', N'OA_FORM_PROPERTIES', N'column', N'FIELD_NAME';
EXECUTE sp_addextendedproperty N'MS_Description', N'表单项唯一值', N'user', N'dbo', N'table', N'OA_FORM_PROPERTIES', N'column', N'FIELD_MD5';
EXECUTE sp_addextendedproperty N'MS_Description', N'表单项类型', N'user', N'dbo', N'table', N'OA_FORM_PROPERTIES', N'column', N'FIELD_TYPE';
EXECUTE sp_addextendedproperty N'MS_Description', N'表单项校验规则', N'user', N'dbo', N'table', N'OA_FORM_PROPERTIES', N'column', N'FIELD_VALID';
EXECUTE sp_addextendedproperty N'MS_Description', N'表单项创建时间', N'user', N'dbo', N'table', N'OA_FORM_PROPERTIES', N'column', N'CREATE_TIME';

if exists ( select *
 from  sysobjects
 where name = 'OA_CONTRACT_CIRCULATION'
 and type = 'U')
 drop table OA_CONTRACT_CIRCULATION
go
CREATE TABLE OA_CONTRACT_CIRCULATION
(
  CONTRACT_ID     INT IDENTITY PRIMARY KEY,
  TEMPLATE_ID     NVARCHAR(255),
  CONTRACT_REOPEN INT,
  PROCESSINSTANCE_ID NVARCHAR(255) not null,
  CONTRACT_NAME   NVARCHAR(255),
  USER_ID         INT,
  CONTRACT_STATUS NVARCHAR(255),
  DESCRIPTION     NVARCHAR(255),
  CONTRACT_HTML   TEXT,
  CONTRACT_PDF    IMAGE,
  WORK_STATUS     INT,
  CREATE_TIME     DATETIME
)
GO

/* 表注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'合同流转表', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', NULL, NULL;
/* 字段注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'合同编号', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'CONTRACT_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'模板编号', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'TEMPLATE_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'流程实例编号', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'PROCESSINSTANCE_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'合同名称', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'CONTRACT_NAME';
EXECUTE sp_addextendedproperty N'MS_Description', N'用户编号', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'USER_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'合同流转状态', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'CONTRACT_STATUS';
EXECUTE sp_addextendedproperty N'MS_Description', N'合同描述', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'DESCRIPTION';
EXECUTE sp_addextendedproperty N'MS_Description', N'合同原始HTML', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'CONTRACT_HTML';
EXECUTE sp_addextendedproperty N'MS_Description', N'合同PDF', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'CONTRACT_PDF';
EXECUTE sp_addextendedproperty N'MS_Description', N'开工状态', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'WORK_STATUS';
EXECUTE sp_addextendedproperty N'MS_Description', N'合同创建时间', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'CREATE_TIME';

if exists ( select *
 from  sysobjects
 where name = 'OA_AUDIT'
 and type = 'U')
 drop table OA_AUDIT
go
CREATE TABLE OA_AUDIT
(
  AUDIT_ID     INT IDENTITY PRIMARY KEY,
  USER_NAME     NVARCHAR(255),
  CONTENT     NVARCHAR(500),
  CREATE_TIME     DATETIME
)
GO
/* 表注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'操作审计表', N'user', N'dbo', N'table', N'OA_AUDIT', NULL, NULL;
/* 字段注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'操作审计编号', N'user', N'dbo', N'table', N'OA_AUDIT', N'column', N'AUDIT_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'操作内容', N'user', N'dbo', N'table', N'OA_AUDIT', N'column', N'CONTENT';
EXECUTE sp_addextendedproperty N'MS_Description', N'操作时间', N'user', N'dbo', N'table', N'OA_AUDIT', N'column', N'CREATE_TIME';


-- if exists ( select *
--  from  sysobjects
--  where name = 'OA_ORGANIZATION'
--  and type = 'U')
--  drop table OA_ORGANIZATION
-- go
-- CREATE TABLE OA_ORGANIZATION
-- (
--   ORGANIZATION_ID     INT IDENTITY PRIMARY KEY,
--   ORGANIZATION_NAME     NVARCHAR(255),
--   USER_ID     INT,
--   DESCRIBE         NVARCHAR(500),
--   CREATE_TIME     DATETIME
-- )
--
-- GO
-- /* 表注释 */
-- EXECUTE sp_addextendedproperty N'MS_Description', N'组织机构表', N'user', N'dbo', N'table', N'OA_ORGANIZATION', NULL, NULL;
-- /* 字段注释 */
-- EXECUTE sp_addextendedproperty N'MS_Description', N'组织编号', N'user', N'dbo', N'table', N'OA_ORGANIZATION', N'column', N'ORGANIZATION_ID';
-- EXECUTE sp_addextendedproperty N'MS_Description', N'组织名称', N'user', N'dbo', N'table', N'OA_ORGANIZATION', N'column', N'ORGANIZATION_NAME';
-- EXECUTE sp_addextendedproperty N'MS_Description', N'用户编号', N'user', N'dbo', N'table', N'OA_ORGANIZATION', N'column', N'USER_ID';
-- EXECUTE sp_addextendedproperty N'MS_Description', N'描述信息', N'user', N'dbo', N'table', N'OA_ORGANIZATION', N'column', N'DESCRIBE';
-- EXECUTE sp_addextendedproperty N'MS_Description', N'创建时间', N'user', N'dbo', N'table', N'OA_ORGANIZATION', N'column', N'CREATE_TIME';

if exists ( select *
 from  sysobjects
 where name = 'OA_GROUP'
 and type = 'U')
 drop table OA_GROUP
go
CREATE TABLE OA_GROUP
(
  GROUP_ID     INT IDENTITY PRIMARY KEY,
  GROUP_NAME     NVARCHAR(255),
  USER_ID        INT,
  PRIVILEGEIDS   NVARCHAR(500),
  DESCRIBE       NVARCHAR(500),
  STATUS         INT,
  CREATE_TIME     DATETIME
)
GO
/* 表注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'组织机构表', N'user', N'dbo', N'table', N'OA_GROUP', NULL, NULL;
/* 字段注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'组织编号', N'user', N'dbo', N'table', N'OA_GROUP', N'column', N'GROUP_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'组织名称', N'user', N'dbo', N'table', N'OA_GROUP', N'column', N'GROUP_NAME';
EXECUTE sp_addextendedproperty N'MS_Description', N'创建组用户编号', N'user', N'dbo', N'table', N'OA_GROUP', N'column', N'USER_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'权限信息记录', N'user', N'dbo', N'table', N'OA_GROUP', N'column', N'PRIVILEGEIDS';
EXECUTE sp_addextendedproperty N'MS_Description', N'描述信息', N'user', N'dbo', N'table', N'OA_GROUP', N'column', N'DESCRIBE';
EXECUTE sp_addextendedproperty N'MS_Description', N'组启用状态', N'user', N'dbo', N'table', N'OA_GROUP', N'column', N'STATUS';
EXECUTE sp_addextendedproperty N'MS_Description', N'创建时间', N'user', N'dbo', N'table', N'OA_GROUP', N'column', N'CREATE_TIME';

if exists ( select *
 from  sysobjects
 where name = 'OA_PRIVILEGE'
 and type = 'U')
 drop table OA_PRIVILEGE
go
CREATE TABLE OA_PRIVILEGE
(
  PRIVILEGE_ID     INT IDENTITY PRIMARY KEY,
  TYPE             NVARCHAR(100),
  NAME             NVARCHAR(100),
  CONTENT          NVARCHAR(500),
  DESCRIBE         NVARCHAR(500),
  CREATE_TIME      DATETIME
)
GO
/* 表注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'权限表', N'user', N'dbo', N'table', N'OA_PRIVILEGE', NULL, NULL;
/* 字段注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'权限编号', N'user', N'dbo', N'table', N'OA_PRIVILEGE', N'column', N'PRIVILEGE_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'权限类型', N'user', N'dbo', N'table', N'OA_PRIVILEGE', N'column', N'TYPE';
EXECUTE sp_addextendedproperty N'MS_Description', N'权限名称', N'user', N'dbo', N'table', N'OA_PRIVILEGE', N'column', N'NAME';
EXECUTE sp_addextendedproperty N'MS_Description', N'权限内容', N'user', N'dbo', N'table', N'OA_PRIVILEGE', N'column', N'CONTENT';
EXECUTE sp_addextendedproperty N'MS_Description', N'描述信息', N'user', N'dbo', N'table', N'OA_PRIVILEGE', N'column', N'DESCRIBE';
EXECUTE sp_addextendedproperty N'MS_Description', N'创建时间', N'user', N'dbo', N'table', N'OA_PRIVILEGE', N'column', N'CREATE_TIME';

INSERT INTO OA_PRIVILEGE (TYPE, NAME,CONTENT, CREATE_TIME) VALUES ('menu','新建用户','user','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('menu','用户组管理','group','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('menu','公司管理','enterprise','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('menu','修改密码','password','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME,CONTENT, CREATE_TIME) VALUES ('menu','合同模板管理','upload','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME,CONTENT, CREATE_TIME) VALUES ('menu','合同模板字段检查','form','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME,CONTENT, CREATE_TIME) VALUES ('menu','工作流定义','modeler','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME,CONTENT, CREATE_TIME) VALUES ('menu','已发布流程','deployment','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME,CONTENT, CREATE_TIME) VALUES ('menu','新合同建立','process','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('menu','待处理申请','myProcess','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME,CONTENT, CREATE_TIME) VALUES ('menu','我发起的申请','initiator','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME,CONTENT, CREATE_TIME) VALUES ('menu','合同审核及批复','pending','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('menu','归档文件查询','complete','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME,CONTENT, CREATE_TIME) VALUES ('menu','个人任务统计','privateReport','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('menu','法务任务统计','fawuReport','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('menu','管理员日志查询','audit','2018-04-26 11:41:53.760');

INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('button','新建用户','user','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('button','用户组管理','group','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('button','公司管理','enterprise','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('button','修改密码','password','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('button','合同模板管理','upload','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME,CONTENT, CREATE_TIME) VALUES ('button','合同模板字段检查','form','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('button','工作流定义','modeler','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('button','已发布流程','deployment','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME,CONTENT, CREATE_TIME) VALUES ('button','新合同建立','process','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME,CONTENT, CREATE_TIME) VALUES ('button','待处理申请','myProcess','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('button','我发起的申请','initiator','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('button','合同审核及批复','pending','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME,CONTENT, CREATE_TIME) VALUES ('button','归档文件查询','complete','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME,CONTENT, CREATE_TIME) VALUES ('button','个人任务统计','privateReport','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('button','法务任务统计','fawuReport','2018-04-26 11:41:53.760');
INSERT INTO OA_PRIVILEGE (TYPE, NAME, CONTENT,CREATE_TIME) VALUES ('button','管理员日志查询','audit','2018-04-26 11:41:53.760');


if exists ( select *
 from  sysobjects
 where name = 'OA_ENTERPRISE'
 and type = 'U')
 drop table OA_ENTERPRISE
go
CREATE TABLE OA_ENTERPRISE
(
  ENTERPRISE_ID       INT IDENTITY PRIMARY KEY,
  COMPANY_NAME        NVARCHAR(100),
  COMPANY_PROVINCE    NVARCHAR(20),
  COMPANY_CITY          NVARCHAR(50),
  COMPANY_OWNER         NVARCHAR(50),
  OWNER_MOBILE         NVARCHAR(20),
  COMPANY_STATUS      INT,
  CREATE_TIME      DATETIME
)
GO
/* 表注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'企业表', N'user', N'dbo', N'table', N'OA_ENTERPRISE', NULL, NULL;
/* 字段注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'公司编号', N'user', N'dbo', N'table', N'OA_ENTERPRISE', N'column', N'ENTERPRISE_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'公司名称', N'user', N'dbo', N'table', N'OA_ENTERPRISE', N'column', N'COMPANY_NAME';
EXECUTE sp_addextendedproperty N'MS_Description', N'公司所在省区', N'user', N'dbo', N'table', N'OA_ENTERPRISE', N'column', N'COMPANY_PROVINCE';
EXECUTE sp_addextendedproperty N'MS_Description', N'公司所在市区', N'user', N'dbo', N'table', N'OA_ENTERPRISE', N'column', N'COMPANY_CITY';
EXECUTE sp_addextendedproperty N'MS_Description', N'公司负责人', N'user', N'dbo', N'table', N'OA_ENTERPRISE', N'column', N'COMPANY_OWNER';
EXECUTE sp_addextendedproperty N'MS_Description', N'公司负责人手机号码', N'user', N'dbo', N'table', N'OA_ENTERPRISE', N'column', N'OWNER_MOBILE';
EXECUTE sp_addextendedproperty N'MS_Description', N'公司状态', N'user', N'dbo', N'table', N'OA_ENTERPRISE', N'column', N'COMPANY_STATUS';
EXECUTE sp_addextendedproperty N'MS_Description', N'创建时间', N'user', N'dbo', N'table', N'OA_ENTERPRISE', N'column', N'CREATE_TIME';

if exists ( select *
 from  sysobjects
 where name = 'OA_ATTACHMENT'
 and type = 'U')
 drop table OA_ATTACHMENT
go
CREATE TABLE OA_ATTACHMENT
(
  ATTACHMENT_ID       INT IDENTITY PRIMARY KEY,
  CONTRACT_ID         INT,
  FILE_NAME            NVARCHAR(100),
  FILE_CONTENT        IMAGE,
  CREATE_TIME      DATETIME
)
GO
/* 表注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'附件信息表', N'user', N'dbo', N'table', N'OA_ATTACHMENT', NULL, NULL;
/* 字段注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'附件编号', N'user', N'dbo', N'table', N'OA_ATTACHMENT', N'column', N'ATTACHMENT_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'合同编号', N'user', N'dbo', N'table', N'OA_ATTACHMENT', N'column', N'CONTRACT_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'附件名称', N'user', N'dbo', N'table', N'OA_ATTACHMENT', N'column', N'FILE_NAME';
EXECUTE sp_addextendedproperty N'MS_Description', N'附件内容', N'user', N'dbo', N'table', N'OA_ATTACHMENT', N'column', N'FILE_CONTENT';
EXECUTE sp_addextendedproperty N'MS_Description', N'创建时间', N'user', N'dbo', N'table', N'OA_ATTACHMENT', N'column', N'CREATE_TIME';
-- /*表注释*/
-- SELECT [ColumnName] = [Columns].name ,
--        [Description] = [Properties].value,
--        [SystemTypeName] = [Types].name ,
--        [Precision] = [Columns].precision ,
--        [Scale] = [Columns].scale ,
--        [MaxLength] = [Columns].max_length ,
--        [IsNullable] = [Columns].is_nullable ,
--        [IsRowGUIDCol] = [Columns].is_rowguidcol ,
--        [IsIdentity] = [Columns].is_identity ,
--        [IsComputed] = [Columns].is_computed ,
--        [IsXmlDocument] = [Columns].is_xml_document
-- FROM    sys.tables AS [Tables]
--   INNER JOIN sys.columns AS [Columns] ON [Tables].object_id = [Columns].object_id
--   INNER JOIN sys.types AS [Types] ON [Columns].system_type_id = [Types].system_type_id
--                                      AND is_user_defined = 0
--                                      AND [Types].name <> 'sysname'
--   LEFT OUTER JOIN sys.extended_properties AS [Properties] ON [Properties].major_id = [Tables].object_id
--                                                              AND [Properties].minor_id = [Columns].column_id
--                                                              AND [Properties].name = 'MS_Description'
-- WHERE   [Tables].name ='OA_USER' -- and [Columns].name = '字段名'
-- ORDER BY [Columns].column_id