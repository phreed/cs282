package course.examples.ContentProviders.myContentProvider;

class DataRecord 
{
    static int id;
    private final String _data;
    private final int _id;

    DataRecord(String _data) {
        this._data = _data;
        this._id = ++id;
    }

    DataRecord(int id, String _data) {
        this._data = _data;
        this._id = id;
    }

    String get_data() {
        return _data;
    }

    int get_id() {
        return _id;
    }
}
