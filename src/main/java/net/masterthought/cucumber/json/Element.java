package net.masterthought.cucumber.json;

import static com.googlecode.totallylazy.Option.option;

import java.util.ArrayList;
import java.util.List;

import net.masterthought.cucumber.ConfigurationOptions;
import net.masterthought.cucumber.util.Status;
import net.masterthought.cucumber.util.Util;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.googlecode.totallylazy.Function1;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;

public class Element {

    /** Refers to background step. Is defined in json file. */
    private final static String BACKGROUND_KEYWORD = "Background";

    private String name;
    private String description;
    private String keyword;
    private Step[] steps;
    private Tag[] tags;
    private String imagePath;

    public Element() {
    }

    public Sequence<Step> getSteps() {
        return Sequences.sequence(option(steps).getOrElse(new Step[]{})).realise();
    }

    public Sequence<Tag> getTags() {
        return Sequences.sequence(option(tags).getOrElse(new Tag[]{})).realise();
    }

    public Status getStatus() {
        boolean hasNoFailed = getSteps().filter(Step.predicates.hasStatus(Status.FAILED)).isEmpty();
        if (!hasNoFailed) {
            return Status.FAILED;
        }

        ConfigurationOptions configuration = ConfigurationOptions.instance();
        if (configuration.skippedFailsBuild()) {
            boolean hasNoSkipped = getSteps().filter(Step.predicates.hasStatus(Status.SKIPPED)).isEmpty();
            if (!hasNoSkipped) {
                return Status.FAILED;
            }
        }

        if (configuration.pendingFailsBuild()) {
            boolean hasNoSkipped = getSteps().filter(Step.predicates.hasStatus(Status.PENDING)).isEmpty();
            if (!hasNoSkipped) {
                return Status.FAILED;
            }
        }

        if (configuration.undefinedFailsBuild()) {
            boolean hasNoSkipped = getSteps().filter(Step.predicates.hasStatus(Status.UNDEFINED)).isEmpty();
            if (!hasNoSkipped) {
                return Status.FAILED;
            }
        }

        if (configuration.missingFailsBuild()) {
            boolean hasNoMissing = getSteps().filter(Step.predicates.hasStatus(Status.MISSING)).isEmpty();
            if (!hasNoMissing) {
                return Status.FAILED;
            }
        }
        
        return Status.PASSED;
    }

    public String getRawName() {
        return name;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getName() {
        List<String> contentString = new ArrayList<String>();

        if (Util.itemExists(keyword)) {
            contentString.add("<span class=\"scenario-keyword\">" + StringEscapeUtils.escapeHtml(keyword) + ": </span>");
        }

        if (Util.itemExists(name)) {
            contentString.add("<span class=\"scenario-name\">" + StringEscapeUtils.escapeHtml(name) + "</span>");
        }

        return Util.itemExists(contentString) ? getStatus().toHtmlClass()
                + StringUtils.join(contentString.toArray(), " ") + Util.closeDiv() : "";
    }

    public Sequence<String> getTagList() {
        return processTags();
    }

    public boolean hasTags() {
        return Util.itemExists(tags);
    }

    public boolean hasSteps() {
        return !getSteps().isEmpty();
    }

    private Sequence<String> processTags() {
        return getTags().map(Tag.functions.getName());
    }

    public boolean isBackground() {
        return keyword.equals(BACKGROUND_KEYWORD);
    }

    public String getTagsList() {
        String result = "<div class=\"feature-tags\"></div>";
        if (Util.itemExists(tags)) {
            List<String> str = getTagList().toList();
            List<String> tagList = new ArrayList<String>();
            for(String s : str) {
                String link = s.replace("@", "").trim() + ".html";
                String ref = "<a href=\"" + link + "\">" + s + "</a>";
                tagList.add(ref);
            }
            result = "<div class=\"feature-tags\">" + StringUtils.join(tagList.toArray(), ",") + "</div>";
        }
        return result;
    }

    public static class Functions {
        public static Function1<Element, Status> status() {
            return new Function1<Element, Status>() {
                @Override
                public Status call(Element element) throws Exception {
                    return element.getStatus();
                }
            };
        }
    }

	public String getImage() {
		String result = "";
		if (getStatus() == Status.FAILED) {
			result = "<br><center><a href='"+ imagePath +"' ><img width='30%' height='30%' src='" + imagePath + "'/></a></center>";
		}
		
		return result;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
    
}
