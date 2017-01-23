package com.bsdsolutions.sanjaydixit.redditreader.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class PostItemList {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<SinglePost> ITEMS = new ArrayList<SinglePost>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, SinglePost> ITEM_MAP = new HashMap<String, SinglePost>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(SinglePost item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static SinglePost createDummyItem(int position) {
        return new SinglePost(String.valueOf(position), "Item " + position, "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d7/Android_robot.svg/2000px-Android_robot.svg.png", 100, 100);
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class SinglePost {
        public final String id;
        public final String title;
        public final String image;
        public final int commentCount;
        public final int voteCount;

        public SinglePost(String id, String title, String image, int commentCount, int voteCount) {
            this.id = id;
            this.title = title;
            this.image = image;
            this.commentCount = commentCount;
            this.voteCount = voteCount;
        }

        @Override
        public String toString() {
            return title + " has " + String.valueOf(commentCount) + "comments and " + String.valueOf(voteCount) + " votes.";
        }
    }
}
