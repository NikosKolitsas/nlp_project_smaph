package annotatorstub.main.deprecated;

import annotatorstub.annotator.baseline.BaselineAnnotator;
import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

import java.io.IOException;
import java.util.HashSet;

public class AnnotatorMain {

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		// Create annotator
        // FakeAnnotator ann = new FakeAnnotator(); // Only takes the first word and looks up directly using title
		//BasicAnnotator ann = new BasicAnnotator(); // Splits using spaces and looks up each word directly using title
		BaselineAnnotator ann = new BaselineAnnotator(); // Splits using spaces and follows baseline requirements

        // Sample query
		String query = "magnolia springs whatever obama michelle";

        // Annotate query (INFO should show results)
		HashSet<Annotation> annotations = ann.solveA2W(query);

        // Flush wikipedia API buffer
		WikipediaApiInterface.api().flush();

	}

}
