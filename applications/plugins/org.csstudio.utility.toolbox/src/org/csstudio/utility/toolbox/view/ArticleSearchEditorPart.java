package org.csstudio.utility.toolbox.view;

import java.util.List;

import org.csstudio.utility.toolbox.actions.OpenArticleEditorAction;
import org.csstudio.utility.toolbox.entities.Article;
import org.csstudio.utility.toolbox.framework.controller.SearchController;
import org.csstudio.utility.toolbox.framework.jpa.OrderBy;
import org.csstudio.utility.toolbox.framework.property.Property;
import org.csstudio.utility.toolbox.framework.searchterm.SearchTerm;
import org.csstudio.utility.toolbox.framework.template.AbstractSearchEditorPartTemplate;
import org.csstudio.utility.toolbox.services.ArticleService;
import org.csstudio.utility.toolbox.view.forms.ArticleGuiForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import com.google.inject.Inject;

public class ArticleSearchEditorPart extends AbstractSearchEditorPartTemplate<Article> implements SearchController<Article> {

	public static final String ID = "org.csstudio.utility.toolbox.view.ArticleSearchEditorPart";

	@Inject
	private ArticleGuiForm articleGuiForm;

	@Inject
	private ArticleService articleService;

	@Inject
	private OpenArticleEditorAction openArticleEditorAction;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input, articleGuiForm);
		setPartName(getTitle());
	}

	@Override
	public void createPartControl(Composite composite) {
		articleGuiForm.createSearchPart(composite, getEditorInput(), this);
		setFocusWidget(articleGuiForm.getFocusWidget());
	}

	@Override
	public void executeSearch(List<SearchTerm> searchTerms) {
		List<Article> articles = articleService.find(searchTerms, new OrderBy("id"));
		setSearchPartName(articles.size());		
		articleGuiForm.createSearchResultTableView(getTableViewProvider(), articles,
					Property.createList("beschreibung", "status", "internId"));	
	}

	@Override
	public void create() {
		openArticleEditorAction.runWith(new Article());
	}

	public void openRow(Article article) {
		openArticleEditorAction.runWith(article);
	}

}
