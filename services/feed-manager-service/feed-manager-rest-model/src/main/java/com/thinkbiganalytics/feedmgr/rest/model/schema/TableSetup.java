package com.thinkbiganalytics.feedmgr.rest.model.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.db.model.schema.Field;
import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.feedmgr.metadata.MetadataField;
import com.thinkbiganalytics.feedmgr.rest.model.HadoopSecurityGroup;
import com.thinkbiganalytics.policy.rest.model.FieldPolicy;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by sr186054 on 2/12/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TableSetup {

    private static final Logger log = LoggerFactory.getLogger(TableSetup.class);

    private TableSchema tableSchema;

    private TableSchema sourceTableSchema;

    private String method;

    private String description = "";

    private List<FieldPolicy> fieldPolicies;

    private List<PartitionField> partitions;

    private String tableType;

    @MetadataField
    private String incrementalDateField;

    @MetadataField(description = "Source Field to be used when incrementally querying Table Data ")
    private String sourceTableIncrementalDateField;

    private TableOptions options;

    @MetadataField(description = "Hive Row Format String for the Feed Table (example: ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' STORED AS\n  TEXTFILE ")
    private String feedFormat;

    @MetadataField(description = "Format of the Destination Table storage. Supported Values are: [STORED AS PARQUET, STORED AS ORC]")
    private String targetFormat;

    @MetadataField(description = "Destination Hive Table Properties string (i.e.  tblproperties(\"orc.compress\"=\"SNAPPY\") ")
    private String targetTblProperties;

    @MetadataField(description = "Strategy for merging data into the destination.  Supported Values are [Sync, Merge, Dedupe and Merge]")
    private String targetMergeStrategy;

    @MetadataField(description = "JSON array of FieldPolicy objects")
    private String fieldPoliciesJson;

    @MetadataField(description = "Nifi propety name 'elasticsearch.columns'")
    private String fieldIndexString;

    @MetadataField(description = "Nifi property name 'table_partition_specs'")
    private String partitionStructure;

    @MetadataField(description = "Nifi property name 'partition_specs'")
    private String partitionSpecs;

    @MetadataField(description = "Nifi property name 'table_column_specs'")
    public String fieldStructure;

    @MetadataField(description = "List of destination (feed table) field names separated by a new line")
    private String fieldsString;

    @MetadataField(description = "List of source table field names separated by a new line")
    private String sourceFields;

    @Deprecated
    //this is now referenced in the sourceTableSchema.name
    //${metadata.table.existingTableName} will still work, but it is advised to switch it to ${metadata.table.sourceTableSchema.name}
    public String existingTableName;


    @MetadataField(description = "List of fields that can be null separated by a comma")
    private String nullableFields;

    @MetadataField(description = "List of fields that are primary keys separated by a comma")
    private String primaryKeyFields;

    private List<HadoopSecurityGroup> securityGroups;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<FieldPolicy> getFieldPolicies() {
        return fieldPolicies;
    }

    public void setFieldPolicies(List<FieldPolicy> fieldPolicies) {
        this.fieldPolicies = fieldPolicies;
    }

    public List<PartitionField> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<PartitionField> partitions) {
        this.partitions = partitions;
    }

    public String getPartitionStructure() {
        return partitionStructure;
    }

    public void setPartitionStructure(String partitionStructure) {
        this.partitionStructure = partitionStructure;
    }

    public void setFieldStructure(String fieldStructure) {
        this.fieldStructure = fieldStructure;
    }

    public String getFieldStructure() {
        return fieldStructure;
    }

    public String getFieldsString() {
        return fieldsString;
    }

    public void setFieldsString(String fieldsString) {
        this.fieldsString = fieldsString;
    }

    private void setStringBuffer(StringBuffer sb, String name, String separator) {
        if (StringUtils.isNotBlank(sb.toString())) {
            sb.append(separator);
        }
        sb.append(name);
    }


    @JsonIgnore
    public void updateFieldStringData() {
        StringBuffer fieldsString = new StringBuffer();
        StringBuffer nullableFieldsString = new StringBuffer();
        StringBuffer primaryKeyFieldsString = new StringBuffer();
        if (tableSchema != null && tableSchema.getFields() != null) {
            for (Field field : tableSchema.getFields()) {
                setStringBuffer(fieldsString, field.getName(), "\n");
                if (field.getNullable()) {
                    setStringBuffer(nullableFieldsString, field.getName(), ",");
                }
                if (field.getPrimaryKey()) {
                    setStringBuffer(primaryKeyFieldsString, field.getName(), ",");
                }
            }
        }
        setFieldsString(fieldsString.toString());
        setNullableFields(nullableFieldsString.toString());
        setPrimaryKeyFields(primaryKeyFieldsString.toString());
    }


    @JsonIgnore
    public void updateSourceFieldsString() {
        StringBuffer sb = new StringBuffer();
        if (sourceTableSchema != null && sourceTableSchema.getFields() != null) {
            for (Field field : sourceTableSchema.getFields()) {
                setStringBuffer(sb, field.getName(), "\n");

            }
        }
        setSourceFields(sb.toString());
    }

    @JsonIgnore
    public void updateFieldStructure() {
        StringBuffer sb = new StringBuffer();
        if (tableSchema != null && tableSchema.getFields() != null) {
            for (Field field : tableSchema.getFields()) {
                if (StringUtils.isNotBlank(sb.toString())) {
                    sb.append("\n");
                }
                sb.append(field.asFieldStructure());

            }
        }
        setFieldStructure(sb.toString());
    }

    @JsonIgnore
    public void updateFieldIndexString() {
        StringBuffer sb = new StringBuffer();
        if (tableSchema != null && tableSchema.getFields() != null && fieldPolicies != null) {
            int idx = 0;
            for (FieldPolicy field : fieldPolicies) {
                if (field.isIndex() && StringUtils.isNotBlank(sb.toString())) {
                    sb.append(",");
                }
                if (field.isIndex()) {
                    sb.append(tableSchema.getFields().get(idx).getName());
                }
                idx++;
            }
        }
        fieldIndexString = sb.toString();
    }

    @JsonIgnore
    public void updateFieldPolicyNames() {
        if (tableSchema != null && tableSchema.getFields() != null && fieldPolicies != null) {
            int idx = 0;
            for (FieldPolicy field : fieldPolicies) {
                field.setFieldName(tableSchema.getFields().get(idx).getName());
                idx++;
            }
        }
    }


    @JsonIgnore
    public void updatePartitionStructure() {
        StringBuffer sb = new StringBuffer();
        if (partitions != null) {
            for (PartitionField field : partitions) {
                if (StringUtils.isNotBlank(sb.toString())) {
                    sb.append("\n");
                }
                sb.append(field.asPartitionStructure());

            }
        }
        setPartitionStructure(sb.toString());
    }


    @JsonIgnore
    public void updatePartitionSpecs() {
        StringBuffer sb = new StringBuffer();
        if (partitions != null) {
            for (PartitionField field : partitions) {
                if (StringUtils.isNotBlank(sb.toString())) {
                    sb.append("\n");
                }
                sb.append(field.asPartitionSpec());

            }
        }
        setPartitionSpecs(sb.toString());
    }

    @JsonIgnore
    private void updateFieldPolicyJson() {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[]";
        try {
            json = mapper.writeValueAsString(getFieldPolicies());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        fieldPoliciesJson = json;

    }

    private void updateTargetTblProperties() {
        this.targetTblProperties = "";

        //build based upon compression options
        if (options != null && StringUtils.isNotBlank(options.getCompressionFormat()) && !"NONE".equalsIgnoreCase(options.getCompressionFormat())) {
            if ("STORED AS PARQUET".equalsIgnoreCase(getTargetFormat())) {
                this.targetTblProperties = "tblproperties(\"parquet.compression\"=\"" + options.getCompressionFormat() + "\")";
            } else if ("STORED AS ORC".equalsIgnoreCase(getTargetFormat())) {
                this.targetTblProperties = "tblproperties(\"orc.compress\"=\"" + options.getCompressionFormat() + "\")";
            } else {
                log.warn("Compression enabled with unsupported target format: {}", getTargetFormat());
            }
        }
    }

    public void updateFeedFormat() {
        if (StringUtils.isNotBlank(feedFormat)) {
            feedFormat = StringEscapeUtils.escapeJava(feedFormat); // StringUtils.replace(feedFormat, "\\n", "\\\\n");
        }
    }

    public void updateMetadataFieldValues() {
        updatePartitionStructure();
        updateFieldStructure();
        updateFieldStringData();
        updateSourceFieldsString();
        updateFieldIndexString();
        updatePartitionSpecs();
        updateFieldPolicyNames();
        updateFieldPolicyJson();
        updateTargetTblProperties();
        updateFeedFormat();

    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public TableOptions getOptions() {
        return options;
    }

    public void setOptions(TableOptions options) {
        this.options = options;
    }


    public String getPartitionSpecs() {
        return partitionSpecs;
    }

    public void setPartitionSpecs(String partitionSpecs) {
        this.partitionSpecs = partitionSpecs;
    }

    public TableSchema getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(TableSchema tableSchema) {
        this.tableSchema = tableSchema;
    }

    public String getFieldIndexString() {
        return fieldIndexString;
    }

    public void setFieldIndexString(String fieldIndexString) {
        this.fieldIndexString = fieldIndexString;
    }

    public String getExistingTableName() {
        return existingTableName;
    }

    public void setExistingTableName(String existingTableName) {
        this.existingTableName = existingTableName;
    }

    public String getIncrementalDateField() {
        return incrementalDateField;
    }

    public void setIncrementalDateField(String incrementalDateField) {
        this.incrementalDateField = incrementalDateField;
    }

    public TableSchema getSourceTableSchema() {
        return sourceTableSchema;
    }

    public void setSourceTableSchema(TableSchema sourceTableSchema) {
        this.sourceTableSchema = sourceTableSchema;
    }

    public String getSourceFields() {
        return sourceFields;
    }

    public void setSourceFields(String sourceFields) {
        this.sourceFields = sourceFields;
    }

    public String getNullableFields() {
        return nullableFields;
    }

    public void setNullableFields(String nullableFields) {
        this.nullableFields = nullableFields;
    }

    public String getPrimaryKeyFields() {
        return primaryKeyFields;
    }

    public void setPrimaryKeyFields(String primaryKeyFields) {
        this.primaryKeyFields = primaryKeyFields;
    }

    public String getFieldPoliciesJson() {
        return fieldPoliciesJson;
    }

    public void setFieldPoliciesJson(String fieldPoliciesJson) {
        this.fieldPoliciesJson = fieldPoliciesJson;
    }


    public String getFeedFormat() {
        if (StringUtils.isNotBlank(feedFormat) && feedFormat.contains("\\\\")) {
            return StringUtils.isNotBlank(feedFormat) ? StringEscapeUtils.unescapeJava(feedFormat) : null;
        } else {
            return feedFormat;
        }
    }

    public void setFeedFormat(String feedFormat) {
        this.feedFormat = feedFormat;
    }

    public String getTargetFormat() {
        return targetFormat;
    }

    public void setTargetFormat(String targetFormat) {
        this.targetFormat = targetFormat;
    }

    public String getTargetTblProperties() {
        return targetTblProperties;
    }

    public void setTargetTblProperties(String targetTblProperties) {
        this.targetTblProperties = targetTblProperties;
    }

    public String getSourceTableIncrementalDateField() {
        return sourceTableIncrementalDateField;
    }

    public void setSourceTableIncrementalDateField(String sourceTableIncrementalDateField) {
        this.sourceTableIncrementalDateField = sourceTableIncrementalDateField;
    }

    public String getTargetMergeStrategy() {
        return targetMergeStrategy;
    }

    public void setTargetMergeStrategy(String targetMergeStrategy) {
        this.targetMergeStrategy = targetMergeStrategy;
    }

    public List<HadoopSecurityGroup> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(List<HadoopSecurityGroup> securityGroups) {
        this.securityGroups = securityGroups;
    }
}
