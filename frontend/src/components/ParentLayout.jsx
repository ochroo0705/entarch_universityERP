import { ChildProvider } from './ChildSelector';
import AppShell from './AppShell';

export default function ParentLayout() {
  return (
    <ChildProvider>
      <AppShell role="parent" />
    </ChildProvider>
  );
}
