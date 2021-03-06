// The MIT License (MIT)
//
// Copyright (c) 2015, 2016 Arian Fornaris
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to permit
// persons to whom the Software is furnished to do so, subject to the
// following conditions: The above copyright notice and this permission
// notice shall be included in all copies or substantial portions of the
// Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
// NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
// OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
// USE OR OTHER DEALINGS IN THE SOFTWARE.
package phasereditor.canvas.core;

import static java.lang.System.out;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import phasereditor.assetpack.core.AssetPackCore;
import phasereditor.assetpack.core.IAssetKey;

/**
 * @author arian
 *
 */
public class AssetTable {
	private final List<Entry> _entries;
	private final WorldModel _worldModel;
	private int _counter;
	private final Map<String, IAssetKey> _map;
	private final Map<String, JSONObject> _refMap;

	private static class Entry {

		public Entry(String id, IAssetKey asset) {
			this.id = id;
			this.asset = asset;
		}

		public String id;
		public IAssetKey asset;
	}

	public AssetTable(WorldModel worldModel) {
		_worldModel = worldModel;
		_entries = new ArrayList<>();
		_map = new HashMap<>();
		_refMap = new HashMap<>();
	}

	public String postAsset(IAssetKey key) {
		String key1 = AssetPackCore.uniqueKey(key);
		
		for (Entry entry : _entries) {
			String key2 = AssetPackCore.uniqueKey(entry.asset);

			if (key1.equals(key2)) {
				return entry.id;
			}
		}

		String id = Integer.toString(_counter++);
		_entries.add(new Entry(id, key));
		_map.put(id, key);

		return id;
	}

	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		for (Entry entry : _entries) {
			obj.put(entry.id, AssetPackCore.getAssetJSONReference(entry.asset));
		}
		return obj;
	}

	public void read(JSONObject obj) {
		if (obj == null) {
			return;
		}


		for (String id : obj.keySet()) {
			JSONObject refObj = obj.getJSONObject(id);
			_refMap.put(id, refObj);
			Object asset = AssetPackCore.findAssetElement(_worldModel.getProject(), refObj);
			if (asset instanceof IAssetKey) {
				IAssetKey assetKey = (IAssetKey) asset;
				_entries.add(new Entry(id, assetKey));
				_map.put(id, assetKey);
			} else {
				out.println("Cannot find " + refObj.toString());
				String msg = "section=" + refObj.optString("section") + ", key=" + refObj.optString("asset")
						+ ", frame=" + refObj.optString("sprite", "");
				AssetPackCore.logError("Not found: " + msg);
			}
		}
	}

	public IAssetKey lookup(String id) {
		return _map.get(id);
	}

	public JSONObject getJSONRef(String id) {
		return _refMap.get(id);
	}
}
