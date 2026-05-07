import { Link } from 'react-router-dom';

export default function AnnouncementList({ items, getTitle, getContent, linkBase, emptyLabel }) {
  if (!items.length) {
    return <p className="muted-copy">{emptyLabel}</p>;
  }

  return (
    <div className="stack-list">
      {items.map((item, index) => (
        <Link
          key={item.id}
          to={linkBase ? `${linkBase}/${item.id}` : '#'}
          className={`announcement-item${index === 0 ? ' is-featured' : ''}${!linkBase ? ' is-static' : ''}`}
          onClick={!linkBase ? (event) => event.preventDefault() : undefined}
        >
          <div className="announcement-title">{getTitle(item)}</div>
          <div className="announcement-copy">{getContent(item)}</div>
        </Link>
      ))}
    </div>
  );
}
