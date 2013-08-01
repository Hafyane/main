{* Your footer *}
<div class="span-5"><p><strong>{translate text='Search Options'}</strong></p>
  <ul>
    <li><a href="{$path}/Search/History">{translate text='Search History'}</a></li>
    <li><a href="{$path}/Search/Advanced">{translate text='Advanced Search'}</a></li>
  </ul>
</div>
<div class="span-5"><p><strong>{translate text='Find More'}</strong></p>
  <ul>
    <li><a href="{$path}/Browse/Home">{translate text='Browse the Catalog'}</a></li>
    <li><a href="{$path}/AlphaBrowse/Home">{translate text='Browse Alphabetically'}</a></li>
    <li><a href="{$path}/Search/Reserves">{translate text='Course Reserves'}</a></li>
    <li><a href="{$path}/Search/NewItem">{translate text='New Items'}</a></li>
  </ul>
</div>
<div class="span-5 last"><p><strong>{translate text='Need Help?'}</strong></p>
  <ul>
    <li><a href="{$url}/Help/Home?topic=search" class="searchHelp">{translate text='Search Tips'}</a></li>
    <li><a href="#">{translate text='Ask a Librarian'}</a></li>
    <li><a href="#">{translate text='FAQs'}</a></li>
  </ul>
</div>
<div class="clear"></div>
{* Comply with Serials Solutions terms of service -- this is intentionally left untranslated. *}
{if $module == "Summon"}Powered by Summon™ from Serials Solutions, a division of ProQuest.{/if}