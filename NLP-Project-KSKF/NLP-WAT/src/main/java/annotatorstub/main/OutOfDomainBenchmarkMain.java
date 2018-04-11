package annotatorstub.main;

import annotatorstub.annotator.smaph.SMAPHKSKFAnnotator;
import annotatorstub.utils.Utils;
import it.unipi.di.acube.batframework.cache.BenchmarkCache;
import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.datasetPlugins.YahooWebscopeL24Dataset;
import it.unipi.di.acube.batframework.metrics.Metrics;
import it.unipi.di.acube.batframework.metrics.MetricsResultSet;
import it.unipi.di.acube.batframework.metrics.StrongAnnotationMatch;
import it.unipi.di.acube.batframework.metrics.StrongTagMatch;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.utils.DumpData;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

import java.io.File;
import java.util.HashSet;
import java.util.List;

public class OutOfDomainBenchmarkMain {
    public static void main(String[] args) throws Exception {

        // External handles
        WikipediaApiInterface wikiApi = WikipediaApiInterface.api();

        // Dataset
        String groupName = "KSKF";
        A2WDataset ds = new YahooWebscopeL24Dataset("datasets/out-domain-dataset.xml");

        // Select annotator
        SMAPHKSKFAnnotator ann = new SMAPHKSKFAnnotator(0.1, "python/data/oodomain/oodomain_prediction.csv"); // TODO: THRESHOLD VALUE

        // Annotate C2W
        List<HashSet<Tag>> resTag = BenchmarkCache.doC2WTags(ann, ds);

        SMAPHKSKFAnnotator.reset();

        // Annotate A2W
        List<HashSet<Annotation>> resAnn = BenchmarkCache.doA2WAnnotations(ann, ds);

        // Dump comparison list
        DumpData.dumpCompareList(ds.getTextInstanceList(), ds.getA2WGoldStandardList(), resAnn, wikiApi);

        // Metrics C2W
        Metrics<Tag> metricsTag = new Metrics<>();
        MetricsResultSet C2WRes = metricsTag.getResult(resTag, ds.getC2WGoldStandardList(), new StrongTagMatch(wikiApi));
        Utils.printMetricsResultSet("C2W", C2WRes, ann.getName());

        // Metrics A2W
        Metrics<Annotation> metricsAnn = new Metrics<>();
        MetricsResultSet rsA2W = metricsAnn.getResult(resAnn, ds.getA2WGoldStandardList(), new StrongAnnotationMatch(wikiApi));
        Utils.printMetricsResultSet("A2W-SAM", rsA2W, ann.getName());

        // Write serialized results
        Utils.serializeResult(ann, ds, new File("datasets/annotation-" + groupName + ".bin"));
        wikiApi.flush();

    }
}
