package wenjh.akit.demo.chat.model;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import wenjh.akit.demo.location.model.LatLng;
import wenjh.akit.demo.account.db.BaseDao;
import wenjh.akit.common.util.StringUtil;

public abstract class AbsMessageDao extends BaseDao<Message, String> implements IMessageTable {

	public AbsMessageDao(SQLiteDatabase db, String tableName) {
		super(db, tableName, F_MessageID);
	}

	@Override
	protected Message assemble(Cursor cursor) {
		Message message = new Message();
		assemble(message, cursor);
		return message;
	}

	@Override
	protected void assemble(Message obj, Cursor cursor) {
		obj.setCommunityId(getString(cursor, F_CommunityID));
		obj.setContentType(getInt(cursor, F_ContentType));
		obj.setDistance(getFloat(cursor, F_Distance));
		obj.setDistanceTime(getDate(cursor, F_DistanceTime));
		obj.setMsgId(getString(cursor, F_MessageID));
		obj.setReceived(getBoolean(cursor, F_Received));
		obj.setRemoteUserId(getString(cursor, F_RemoteUserId));
		obj.setStatus(getInt(cursor, F_Status));
		obj.setTextContent(getString(cursor, F_TextContent));
		obj.setTimestamp(getDate(cursor, F_MessageTime));
		
		String mapMessageJsonString = getString(cursor, F_MapMessageLocation);
		if(!StringUtil.isEmpty(mapMessageJsonString)) {
			try {
				JSONObject jsonObject = new JSONObject(mapMessageJsonString);
				LatLng latLng = new LatLng();
				latLng.setAccuracy(jsonObject.optInt("acc"));
				latLng.setLatitude(jsonObject.optDouble("lat"));
				latLng.setLongitude(jsonObject.optDouble("lng"));
				obj.setMapMessageLocation(latLng);
			} catch (JSONException e) {
				log.e(e);
			}
		}
		
		String imgMessageJsonString = getString(cursor, F_Image);
		if(!StringUtil.isEmpty(imgMessageJsonString)) {
			try {
				JSONObject jsonObject = new JSONObject(imgMessageJsonString);
				ChatImage chatImage = new ChatImage();
				String guid = jsonObject.optString("guid");
				if(!StringUtil.isEmpty(guid)) {
					chatImage.setImageGuid(guid);
				} else {
					chatImage.setImageURL(jsonObject.optString("uri"));
					chatImage.uplodedByteSize = jsonObject.optLong("uploaded");
					chatImage.totalByteSize = jsonObject.optLong("total");
				}
				obj.setImageContent(chatImage);
			} catch (JSONException e) {
				log.e(e);
			}
		}
	}

	@Override
	public void insert(Message t) {
		Map<String, Object> insertFiledsMap = new HashMap<String, Object>();
		insertFiledsMap.put(F_CommunityID, t.getCommunityId());
		insertFiledsMap.put(F_ContentType, t.getContentType());
		insertFiledsMap.put(F_Distance, t.getDistance());
		insertFiledsMap.put(F_DistanceTime, t.getDistanceTime());
		insertFiledsMap.put(F_MessageID, t.getMsgId());
		insertFiledsMap.put(F_Received, t.isReceived());
		insertFiledsMap.put(F_RemoteUserId, t.getRemoteUserId());
		insertFiledsMap.put(F_Status, t.getStatus());
		insertFiledsMap.put(F_TextContent, t.getTextContent());
		insertFiledsMap.put(F_MessageTime, t.getTimestamp());
		if(t.getMapMessageLocation() != null) {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("lat", t.getMapMessageLocation().getLatitude());
				jsonObject.put("lng", t.getMapMessageLocation().getLongitude());
				jsonObject.put("acc", t.getMapMessageLocation().getAccuracy());
				insertFiledsMap.put(F_MapMessageLocation, jsonObject.toString());
			} catch (JSONException e) {
				log.e(e);
			}
		}
		
		if(t.getImageContent() != null) {
			try {
				JSONObject jsonObject = new JSONObject();
				ChatImage chatImage = t.getImageContent();
				if(chatImage.getImageGuid() != null) {
					jsonObject.put("guid", chatImage.getImageGuid());
				} else {
					jsonObject.put("uri", chatImage.getImageUri());
					jsonObject.put("uploaded", chatImage.uplodedByteSize);
					jsonObject.put("total", chatImage.totalByteSize);
				}
				insertFiledsMap.put(F_Image, jsonObject.toString());
			} catch (JSONException e) {
				log.e(e);
			}
		}
		
		insertFileds(insertFiledsMap);
	}

	@Override
	public void update(Message t) {
		Map<String, Object> insertFiledsMap = new HashMap<String, Object>();
		insertFiledsMap.put(F_CommunityID, t.getCommunityId());
		insertFiledsMap.put(F_ContentType, t.getContentType());
		insertFiledsMap.put(F_Distance, t.getDistance());
		insertFiledsMap.put(F_DistanceTime, t.getDistanceTime());
		insertFiledsMap.put(F_Received, t.isReceived());
		insertFiledsMap.put(F_RemoteUserId, t.getRemoteUserId());
		insertFiledsMap.put(F_Status, t.getStatus());
		insertFiledsMap.put(F_TextContent, t.getTextContent());
		insertFiledsMap.put(F_MessageTime, t.getTimestamp());
		if(t.getMapMessageLocation() != null) {
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("lat", t.getMapMessageLocation().getLatitude());
				jsonObject.put("lng", t.getMapMessageLocation().getLongitude());
				jsonObject.put("acc", t.getMapMessageLocation().getAccuracy());
				insertFiledsMap.put(F_MapMessageLocation, jsonObject.toString());
			} catch (JSONException e) {
				log.e(e);
			}
		}
		if(t.getImageContent() != null) {
			try {
				JSONObject jsonObject = new JSONObject();
				ChatImage chatImage = t.getImageContent();
				if(chatImage.getImageGuid() != null) {
					jsonObject.put("guid", chatImage.getImageGuid());
				} else {
					jsonObject.put("uri", chatImage.getImageUri());
					jsonObject.put("uploaded", chatImage.uplodedByteSize);
					jsonObject.put("total", chatImage.totalByteSize);
				}
				insertFiledsMap.put(F_Image, jsonObject.toString());
			} catch (JSONException e) {
				log.e(e);
			}
		}
		updateFileds(insertFiledsMap, new String[]{F_MessageID}, new String[]{t.getMsgId()});
	}

	@Override
	public void deleteInstence(Message obj) {
		delete(obj.getMsgId());
	}

}
