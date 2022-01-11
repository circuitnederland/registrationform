import org.cyclos.entities.contentmanagement.MenuItem

Long pageId = unmaskId(scriptParameters.pageId)
MenuItem page = entityManagerHandler.find(MenuItem, pageId)
return richTextHandler.replaceTagsByUrls(page.content)
