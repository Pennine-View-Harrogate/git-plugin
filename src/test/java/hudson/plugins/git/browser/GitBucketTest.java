package hudson.plugins.git.browser;

import hudson.plugins.git.GitChangeLogParser;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitChangeSet.Path;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

public class GitBucketTest extends TestCase {

    /**
     *
     */
    private static final String GITBUCKET_URL = "http://myhost/USER/REPO";
    private final GitBucket gitbucket;

    {
        try {
            gitbucket = new GitBucket(GITBUCKET_URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test method for {@link hudson.plugins.git.browser.GitBucket#getUrl()}.
     * @throws MalformedURLException
     */
    public void testGetUrl() throws MalformedURLException {
        assertEquals(String.valueOf(gitbucket.getUrl()), GITBUCKET_URL  + "/");
    }

    /**
     * Test method for {@link hudson.plugins.git.browser.GitBucket#getUrl()}.
     * @throws MalformedURLException
     */
    public void testGetUrlForRepoWithTrailingSlash() throws MalformedURLException {
        assertEquals(String.valueOf(new GitBucket(GITBUCKET_URL + "/").getUrl()), GITBUCKET_URL  + "/");
    }

    /**
     * Test method for {@link hudson.plugins.git.browser.GitBucket#getChangeSetLink(hudson.plugins.git.GitChangeSet)}.
     * @throws SAXException
     * @throws IOException
     */
    public void testGetChangeSetLinkGitChangeSet() throws IOException, SAXException {
        final URL changeSetLink = gitbucket.getChangeSetLink(createChangeSet("rawchangelog"));
        assertEquals(GITBUCKET_URL + "/commit/396fc230a3db05c427737aa5c2eb7856ba72b05d", changeSetLink.toString());
    }

    /**
     * Test method for {@link hudson.plugins.git.browser.GitBucket#getDiffLink(hudson.plugins.git.GitChangeSet.Path)}.
     * @throws SAXException
     * @throws IOException
     */
    public void testGetDiffLinkPath() throws IOException, SAXException {
        final HashMap<String, Path> pathMap = createPathMap("rawchangelog");
        final Path path1 = pathMap.get("src/main/java/hudson/plugins/git/browser/GithubWeb.java");
        assertEquals(GITBUCKET_URL + "/commit/396fc230a3db05c427737aa5c2eb7856ba72b05d#diff-0", gitbucket.getDiffLink(path1).toString());
        final Path path2 = pathMap.get("src/test/java/hudson/plugins/git/browser/GithubWebTest.java");
        assertEquals(GITBUCKET_URL + "/commit/396fc230a3db05c427737aa5c2eb7856ba72b05d#diff-1", gitbucket.getDiffLink(path2).toString());
        final Path path3 = pathMap.get("src/test/resources/hudson/plugins/git/browser/rawchangelog-with-deleted-file");
        assertNull("Do not return a diff link for added files.", gitbucket.getDiffLink(path3));
    }

    /**
     * Test method for {@link hudson.plugins.git.browser.GitBucket#getFileLink(hudson.plugins.git.GitChangeSet.Path)}.
     * @throws SAXException
     * @throws IOException
     */
    public void testGetFileLinkPath() throws IOException, SAXException {
        final HashMap<String,Path> pathMap = createPathMap("rawchangelog");
        final Path path = pathMap.get("src/main/java/hudson/plugins/git/browser/GithubWeb.java");
        final URL fileLink = gitbucket.getFileLink(path);
        assertEquals(GITBUCKET_URL  + "/blob/396fc230a3db05c427737aa5c2eb7856ba72b05d/src/main/java/hudson/plugins/git/browser/GithubWeb.java", String.valueOf(fileLink));
    }

    /**
     * Test method for {@link hudson.plugins.git.browser.GitBucket#getFileLink(hudson.plugins.git.GitChangeSet.Path)}.
     * @throws SAXException
     * @throws IOException
     */
    public void testGetFileLinkPathForDeletedFile() throws IOException, SAXException {
        final HashMap<String,Path> pathMap = createPathMap("rawchangelog-with-deleted-file");
        final Path path = pathMap.get("bar");
        final URL fileLink = gitbucket.getFileLink(path);
        assertEquals(GITBUCKET_URL + "/commit/fc029da233f161c65eb06d0f1ed4f36ae81d1f4f#diff-0", String.valueOf(fileLink));
    }

    private GitChangeSet createChangeSet(String rawchangelogpath) throws IOException, SAXException {
        final File rawchangelog = new File(GitBucketTest.class.getResource(rawchangelogpath).getFile());
        final GitChangeLogParser logParser = new GitChangeLogParser(false);
        final List<GitChangeSet> changeSetList = logParser.parse(null, rawchangelog).getLogs();
        return changeSetList.get(0);
    }

    /**
     * @param changelog
     * @return
     * @throws IOException
     * @throws SAXException
     */
    private HashMap<String, Path> createPathMap(final String changelog) throws IOException, SAXException {
        final HashMap<String, Path> pathMap = new HashMap<String, Path>();
        final Collection<Path> changeSet = createChangeSet(changelog).getPaths();
        for (final Path path : changeSet) {
            pathMap.put(path.getPath(), path);
        }
        return pathMap;
    }


}
