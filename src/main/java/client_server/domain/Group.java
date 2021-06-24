package client_server.domain;

import org.json.JSONObject;

import java.util.List;

public class Group {

    private final Integer id;
    private final String name;
    private final String description;

    public Group(JSONObject group){
        this.id = group.getInt("id");
        this.name = group.getString("name");
        this.description = group.getString("description");
    }

    public Group(final Integer id, final String name, final String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() { return description; }

    @Override
    public String toString(){
        return "id: "+id+", name: "+name;
    }

    public JSONObject toJSON(){
        return new JSONObject("{"+"\"id\":"+id+", \"name\":\""+name+"\", \"description\":\""+description+"\"}");
    }


    public static JSONObject toJSONObject(List<Group> groups) {
        StringBuilder stringBuffer = new StringBuilder();

        stringBuffer.append("{\"list\":[");

        for (Group g : groups) {
            stringBuffer.append(g.toJSON().toString()).append(", ");
        }
        stringBuffer.delete(stringBuffer.length() - 1, stringBuffer.length() - 1);
        stringBuffer.append("]}");

        return new JSONObject(stringBuffer.toString());
    }
}
