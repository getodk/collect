package org.odk.collect.android.amazonaws.models.nosql;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "fieldtask-mobilehub-447720176-devices")

public class DevicesDO {
    private String _registrationId;
    private String _smapServer;
    private String _userIdent;

    @DynamoDBHashKey(attributeName = "registrationId")
    @DynamoDBAttribute(attributeName = "registrationId")
    public String getRegistrationId() {
        return _registrationId;
    }

    public void setRegistrationId(final String _registrationId) {
        this._registrationId = _registrationId;
    }
    @DynamoDBAttribute(attributeName = "smapServer")
    public String getSmapServer() {
        return _smapServer;
    }

    public void setSmapServer(final String _smapServer) {
        this._smapServer = _smapServer;
    }
    @DynamoDBAttribute(attributeName = "userIdent")
    public String getUserIdent() {
        return _userIdent;
    }

    public void setUserIdent(final String _userIdent) {
        this._userIdent = _userIdent;
    }

}
