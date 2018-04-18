CREATE TABLE OA_USER
(
  USER_ID           INT IDENTITY PRIMARY KEY,
  USER_NAME         NVARCHAR(200) NOT NULL ,
  USER_MOBILE       NVARCHAR(20),
  USER_EMAIL        NVARCHAR(200),
  USER_PWD          NVARCHAR(200),
  USER_COMPANY      NVARCHAR(200),
  USER_DEPARTMENT   NVARCHAR(200),
  USER_POSITION     NVARCHAR(200),
  USER_ADDRESS      NVARCHAR(200),
  USER_POSTCODE     NVARCHAR(200),
  USER_WEIXIN       NVARCHAR(100),
  USER_STATUS       INT,
  USER_CREATETIME   DATETIME
)
GO
/* 表注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'用户表', N'user', N'dbo', N'table', N'OA_USER', NULL, NULL;
/* 字段注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'用户编号', N'user', N'dbo', N'table', N'OA_USER', N'column', N'USER_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'用户名称', N'user', N'dbo', N'table', N'OA_USER', N'column', N'USER_NAME';
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

CREATE TABLE OA_CONTRACT_TEMPLATE
(
  TEMPLATE_ID         INT IDENTITY PRIMARY KEY,
  TEMPLATE_NAME       NVARCHAR(255),
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


CREATE TABLE OA_FORM_PROPERTIES
(
  PROPERTIES_ID INT IDENTITY PRIMARY KEY,
  TEMPLATE_ID   INT,
  FIELD_NAME    NVARCHAR(255),
  FIELD_MD5     NVARCHAR(255) NOT NULL,
  FIELD_TYPE    NVARCHAR(255),
  FIELD_VALID   NVARCHAR(255),
  CREATE_TIME   DATETIME
)
GO

/* 表注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'合同模板表单项', N'user', N'dbo', N'table', N'OA_FORM_PROPERTIES', NULL, NULL;
/* 字段注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'表单项编号', N'user', N'dbo', N'table', N'OA_FORM_PROPERTIES', N'column', N'PROPERTIES_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'合同模板编号', N'user', N'dbo', N'table', N'OA_FORM_PROPERTIES', N'column', N'TEMPLATE_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'表单项名称', N'user', N'dbo', N'table', N'OA_FORM_PROPERTIES', N'column', N'FIELD_NAME';
EXECUTE sp_addextendedproperty N'MS_Description', N'表单项类型', N'user', N'dbo', N'table', N'OA_FORM_PROPERTIES', N'column', N'FIELD_TYPE';
EXECUTE sp_addextendedproperty N'MS_Description', N'表单项校验规则', N'user', N'dbo', N'table', N'OA_FORM_PROPERTIES', N'column', N'FIELD_VALID';
EXECUTE sp_addextendedproperty N'MS_Description', N'表单项创建时间', N'user', N'dbo', N'table', N'OA_FORM_PROPERTIES', N'column', N'CREATE_TIME';

CREATE TABLE OA_CONTRACT_CIRCULATION
(
  CONTRACT_ID     INT IDENTITY PRIMARY KEY,
  TEMPLATE_ID     NVARCHAR(255),
  CONTRACT_NAME   NVARCHAR(255),
  USER_ID         INT,
  CONTRACT_STATUS NVARCHAR(255),
  DESCRIPTION     NVARCHAR(255),
  CONTRACT_HTML   TEXT,
  CONTRACT_PDF    IMAGE,
  CREATE_TIME     DATETIME
)
GO

/* 表注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'合同流转表', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', NULL, NULL;
/* 字段注释 */
EXECUTE sp_addextendedproperty N'MS_Description', N'合同编号', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'CONTRACT_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'合同名称', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'CONTRACT_NAME';
EXECUTE sp_addextendedproperty N'MS_Description', N'用户编号', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'USER_ID';
EXECUTE sp_addextendedproperty N'MS_Description', N'合同状态', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'CONTRACT_STATUS';
EXECUTE sp_addextendedproperty N'MS_Description', N'合同描述', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'DESCRIPTION';
EXECUTE sp_addextendedproperty N'MS_Description', N'合同原始HTML', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'CONTRACT_HTML';
EXECUTE sp_addextendedproperty N'MS_Description', N'合同PDF', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'CONTRACT_PDF';
EXECUTE sp_addextendedproperty N'MS_Description', N'合同创建时间', N'user', N'dbo', N'table', N'OA_CONTRACT_CIRCULATION', N'column', N'CREATE_TIME';

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


/*表注释*/
SELECT [ColumnName] = [Columns].name ,
       [Description] = [Properties].value,
       [SystemTypeName] = [Types].name ,
       [Precision] = [Columns].precision ,
       [Scale] = [Columns].scale ,
       [MaxLength] = [Columns].max_length ,
       [IsNullable] = [Columns].is_nullable ,
       [IsRowGUIDCol] = [Columns].is_rowguidcol ,
       [IsIdentity] = [Columns].is_identity ,
       [IsComputed] = [Columns].is_computed ,
       [IsXmlDocument] = [Columns].is_xml_document
FROM    sys.tables AS [Tables]
  INNER JOIN sys.columns AS [Columns] ON [Tables].object_id = [Columns].object_id
  INNER JOIN sys.types AS [Types] ON [Columns].system_type_id = [Types].system_type_id
                                     AND is_user_defined = 0
                                     AND [Types].name <> 'sysname'
  LEFT OUTER JOIN sys.extended_properties AS [Properties] ON [Properties].major_id = [Tables].object_id
                                                             AND [Properties].minor_id = [Columns].column_id
                                                             AND [Properties].name = 'MS_Description'
WHERE   [Tables].name ='OA_USER' -- and [Columns].name = '字段名'
ORDER BY [Columns].column_id